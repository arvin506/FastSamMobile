package com.example.pytochmobiledemo

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.pytorch.*
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), Runnable {
    private lateinit var mImageView: ImageView
    private lateinit var mImageView2: ImageView
    private lateinit var mButtonSegment: Button
    private lateinit var mProgressBar: ProgressBar
    private var mBitmap: Bitmap? = null
    private lateinit var mModule: Module
    private lateinit var tvDur: TextView
    private lateinit var tvDurMode: TextView

    private var start = 0L
    var index = 0;
    private var names = listOf<String>(
        "dog.jpg",
        "sa_8776.jpg",
        "abc2.jpg",
        "sa_862.jpg",
        "sa_11025.jpg",
        "sa_10039.jpg",
        "a111.jpg",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            mBitmap = BitmapFactory.decodeStream(assets.open(names[0]))
        } catch (e: IOException) {
            Log.e("ImageSegmentation", "Error reading assets", e)
            finish()
        }
        mImageView = findViewById(R.id.imageView)
        mImageView2 = findViewById(R.id.imageView2)
        mImageView.setImageBitmap(mBitmap)
        tvDur = findViewById(R.id.tv_dur)
        tvDurMode = findViewById(R.id.tv_dur_mode)
        val buttonRestart = findViewById<Button>(R.id.restartButton)
        buttonRestart.setOnClickListener {
            try {
                mBitmap = BitmapFactory.decodeStream(assets.open(names[(++index) % names.size]))
                mImageView.setImageBitmap(mBitmap)

                mButtonSegment.isEnabled = true
                mButtonSegment.text = "Segment"

            } catch (e: IOException) {
                Log.e("ImageSegmentation", "Error reading assets", e)
                finish()
            }
        }
        mButtonSegment = findViewById(R.id.segmentButton)
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mButtonSegment.setOnClickListener(View.OnClickListener {
            start = System.currentTimeMillis()
            mButtonSegment.setEnabled(false)
            mProgressBar!!.visibility = ProgressBar.VISIBLE
            mButtonSegment.setText(getString(R.string.run_model))
            val thread = Thread(this@MainActivity)
            thread.start()
        })
        try {
            mModule = PyTorchAndroid.loadModuleFromAsset(assets, "FastSAM-x.ptl", Device.CPU)
        } catch (e: Exception) {
            Log.e("ImageSegmentation", "Error reading assets", e)
            finish()
        }
    }

    override fun run() {
        val scale = Bitmap.createScaledBitmap(mBitmap!!, 1024, 1024, true)


        val inputTensor: Tensor = TensorImageUtils.bitmapToFloat32Tensor(
            mBitmap,
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(1f, 1f, 1f),
        )
//        val inputTensor: Tensor = TensorImageUtils.bitmapToFloat32Tensor(
//            mBitmap,
//            TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//            TensorImageUtils.TORCHVISION_NORM_STD_RGB,
//        )

        val inputs: FloatArray = inputTensor.getDataAsFloatArray()
        val startTime = SystemClock.elapsedRealtime()
        val forward: IValue = mModule.forward(IValue.from(inputTensor))
        val inferenceTime = SystemClock.elapsedRealtime() - startTime
        Log.d("ImageSegmentation", "inference time (ms): $inferenceTime")
        val tuple: Array<IValue> = forward.toTuple()
        val value0: IValue = tuple[0]
        val value1: IValue = tuple[1]
        val tensor0 = value0.toTensor()
        val tensor1: Tensor = value1.toTensor()


        val prediction = tensor0.dataAsFloatArray
        val proto = tensor1.dataAsFloatArray

//        FileUtils.saveFloatArrayToFile(this,"data.txt",prediction)

        Log.e("xiaoyi", "sava success")

        showModeTime()

        val result = Utils.nonMaxSuppression(prediction, proto)

        var masks = result[0] as Array<Array<FloatArray>>
        var boxes = result[1] as Array<FloatArray>


        val bitmap = Bitmap.createBitmap(mBitmap!!.width, mBitmap!!.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

//        boxes.forEachIndexed { index, it ->
//            val color = Utils.randomColor(0.6f)
//            paint.color = color
//            val rect = Rect(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt())
//            canvas.drawRect(rect, paint)
//
//            val m0 = masks[index]
//            for (i in 0..1023) {
//                for (i1 in 0..1023) {
//                    if (m0[i][i1] > 0.5) {
//                        bitmap.setPixel(i1, i, color)
//                    }
//                }
//            }
//        }
        boxes.forEachIndexed { index, it ->
            val color = Utils.randomColor(0.6f)
            paint.color = color
            val rect = Rect(it[0].toInt(), it[1].toInt(), it[2].toInt(), it[3].toInt())
            canvas.drawRect(rect, paint)

            var m0 = masks[index]
            for (i in 0..1023) {
                for (i1 in 0..1023) {
                    if (m0[i][i1] > 0.5) {
                        bitmap.setPixel(i1, i, color)
                    }
                }
            }
        }



        val bitmap2 = Bitmap.createBitmap(mBitmap!!.width, mBitmap!!.height, Bitmap.Config.ARGB_8888)
        val canvas2 = Canvas(bitmap2)
        canvas2.drawBitmap(mBitmap!!, 0f, 0f, null)

        canvas2.drawBitmap(bitmap, 0f, 0f, null)


        updateBitmap(bitmap2!!)

    }

    private fun updateBitmap(bitmap: Bitmap) {
        mImageView2!!.post {
            mImageView2!!.setImageBitmap(bitmap)
            tvDur.setText("总耗时 ： ${System.currentTimeMillis() - start} ms")
            mProgressBar.visibility = View.GONE

        }

    }

    private fun showModeTime() {
        tvDurMode.post {
            tvDurMode.setText("模型耗时： ${System.currentTimeMillis() - start} ms")
        }
    }


    fun floatArrayToGrayscaleBitmap(bgrFloatArray: FloatArray, width: Int, height: Int): Bitmap {
        val length = bgrFloatArray.size
        val rgbFloatArray = FloatArray(length)

// 进行BGR到RGB通道的数据重新排列
        for (y in 0 until height) {
            for (x in 0 until width) {
                // 计算当前像素在BGR float[]数组中的索引
                val bgrIndex = (y * width + x) * 3

                // 计算当前像素在RGB float[]数组中的索引，并进行通道顺序的调整
                val rgbIndex = (y * width + x) * 3
                rgbFloatArray[rgbIndex] = bgrFloatArray[bgrIndex + 2] // 红色通道
                rgbFloatArray[rgbIndex + 1] = bgrFloatArray[bgrIndex + 1] // 绿色通道
                rgbFloatArray[rgbIndex + 2] = bgrFloatArray[bgrIndex] // 蓝色通道
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val red = rgbFloatArray[index]
                val green = rgbFloatArray[index + width * height]
                val blue = rgbFloatArray[index + 2 * width * height]

                // Convert from [0, 1] range to [0, 255]
                val r = (red * 255).toInt()
                val g = (green * 255).toInt()
                val b = (blue * 255).toInt()

                // Combine RGB values into a single pixel color
                val pixelColor = Color.rgb(r, g, b)

                // Set the pixel color in the Bitmap
                bitmap.setPixel(x, y, pixelColor)
            }
        }
        return bitmap
    }

    companion object {
        // see http://host.robots.ox.ac.uk:8080/pascal/VOC/voc2007/segexamples/index.html for the list of classes with indexes
        private const val CLASSNUM = 21
        private const val DOG = 12
        private const val PERSON = 15
        private const val SHEEP = 17

        @Throws(IOException::class)
        fun assetFilePath(context: Context, assetName: String?): String {
            val file = File(context.filesDir, assetName)
            if (file.exists() && file.length() > 0) {
                return file.absolutePath
            }
            context.assets.open(assetName!!).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        }
    }


    fun floatArrayToGrayscaleBitmap(
        tensor: Tensor
    ): Bitmap {
        val floatData = tensor.dataAsFloatArray

        // Get the width and height of the tensor

        // Get the width and height of the tensor
        val width: Int = tensor.shape()[3].toInt()
        val height: Int = tensor.shape()[2].toInt()

        // Create an Android Bitmap with ARGB_8888 configuration

        // Create an Android Bitmap with ARGB_8888 configuration
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        // Iterate through the tensor data and set the corresponding pixel color in the Bitmap

        // Iterate through the tensor data and set the corresponding pixel color in the Bitmap
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x

                // Assuming the tensor is a single-channel grayscale image, set the same value for RGB channels
                val value = (floatData[index] * 255).toInt()
                val pixelColor = -0x1000000 or (value shl 16) or (value shl 8) or value
                bitmap.setPixel(x, y, pixelColor)
            }
        }

        return bitmap
    }
}
