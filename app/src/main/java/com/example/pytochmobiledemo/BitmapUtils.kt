package com.example.pytochmobiledemo

import android.graphics.Bitmap

object BitmapUtils {

    // 将一维 float 数组转换为 Bitmap
    fun convertFloatArrayToBitmap(floatArray: FloatArray, width: Int, height: Int): Bitmap? {
        val pixels = IntArray(width * height)
        var pixelIndex = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                // 从一维数组中获取对应像素的 float 值
                val pixelValue = floatArray[y * width + x]
                // 将 float 值转换为 ARGB 颜色值
                val color = floatToColor(pixelValue)
                pixels[pixelIndex++] = color
            }
        }

        // 创建 Bitmap 对象
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    // 将 float 值转换为 ARGB 颜色值
    private fun floatToColor(floatVal: Float): Int {
        val alpha = 255
        val red = (floatVal * 255).toInt()
        val green = (floatVal * 255).toInt()
        val blue = (floatVal * 255).toInt()
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }
}