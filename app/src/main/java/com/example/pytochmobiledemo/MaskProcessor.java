package com.example.pytochmobiledemo;


import android.graphics.Bitmap;
import android.graphics.Canvas;

public class MaskProcessor {

    public static float[][][] processMaskNative(float[][][] protos, float[][] maskIn, float[][] boxes, int[] shape, boolean sampling) {
        int c = protos[0].length;
        int mh = protos[1].length;
        int mw = protos[2].length;

        float[][] protos_view = ReshapeUtils.reshape(protos);
        float[][] protos_matrix = MatrixMultiplication.matrixMultiplication(maskIn, protos_view);
        float[][] protos_sigmoid = Sigmoid.out(protos_matrix);
        float[][][] masks = ReshapeUtils.reshape(protos_sigmoid, new int[]{protos_sigmoid.length, 256, 256});

        float gain = Math.min(mh * 1.0f / shape[0], mw * 1.0f / shape[1]);
        float[] paid = new float[2];
        paid[0] = (mw - shape[1] * gain) / 2;
        paid[1] = (mh - shape[0] * gain) / 2;

        int top = Math.round(paid[1]);
        int left = Math.round(paid[0]);

        int bottom = mh - top;
        int right = mw - left;

        masks = SplitArrayUtils.splitArray(masks, 0, masks.length, top, bottom - top, left, right - left);

        int dim = 2;
        if (sampling) {
            float[][][] upsampleBilinear2D = UpsampleBilinear2D.upsampleBilinear2D(masks, new int[]{1024, 1024});
            masks = Utils.cropMask(upsampleBilinear2D, boxes);
        } else {
            masks = Utils.cropMask(masks, boxes);
        }
        masks = Utils.gt(masks, 0.5f);
        return masks;
    }

    public static Bitmap createBitmap(float[][][] pixels) {
        Bitmap res = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(res);
        for (int i = 0; i < pixels.length; i++) {
            Bitmap b = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888);
            for (int j = 0; j < pixels[0].length; j++) {
                for (int k = 0; k < pixels[0][0].length; k++) {
                    b.setPixel(j, k, floatToARGB(pixels[i][j][k]));
                }
            }
            c.drawBitmap(b, 0, 0, null);
        }
        return res;
    }

    public static int floatToARGB(float floatValue) {
        // 将浮点数映射到0到255之间的整数值
        int intValue = Math.round(floatValue * 255);

        // 确保整数值在0到255之间
        intValue = Math.max(0, Math.min(intValue, 255));

        // 构建ARGB格式的32位整数，Alpha通道为255（不透明）
        int argbValue = (255 << 24) | (intValue << 16) | (intValue << 8) | intValue;

        return argbValue;
    }
}
