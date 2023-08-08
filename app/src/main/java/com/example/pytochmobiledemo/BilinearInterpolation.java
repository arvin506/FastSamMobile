package com.example.pytochmobiledemo;

import java.util.ArrayList;
import java.util.List;

public class BilinearInterpolation {

    public static float[][][][] upsampleBilinear2D(float[][][][] tensor, int[] shape) {
        int batchSize = tensor.length;
        int channels = tensor[0].length;
        int inputHeight = tensor[0][0].length;
        int inputWidth = tensor[0][0][0].length;
        int outputHeight = shape[0];
        int outputWidth = shape[1];

        float[][][][] output = new float[batchSize][channels][outputHeight][outputWidth];

        for (int b = 0; b < batchSize; b++) {
            for (int c = 0; c < channels; c++) {
                for (int oh = 0; oh < outputHeight; oh++) {
                    for (int ow = 0; ow < outputWidth; ow++) {
                        float ih = oh * (inputHeight - 1) / (float) (outputHeight - 1);
                        float iw = ow * (inputWidth - 1) / (float) (outputWidth - 1);

                        int h0 = (int) Math.floor(ih);
                        int w0 = (int) Math.floor(iw);
                        int h1 = Math.min(h0 + 1, inputHeight - 1);
                        int w1 = Math.min(w0 + 1, inputWidth - 1);

                        float dh = ih - h0;
                        float dw = iw - w0;

                        output[b][c][oh][ow] = lerp(
                                lerp(tensor[b][c][h0][w0], tensor[b][c][h0][w1], dw),
                                lerp(tensor[b][c][h1][w0], tensor[b][c][h1][w1], dw),
                                dh
                        );
                    }
                }
            }
        }

        return output;
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }


}
