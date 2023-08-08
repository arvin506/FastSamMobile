package com.example.pytochmobiledemo;

import java.util.Arrays;

public class UpsampleBilinear2D {

    public static float[][][] upsampleBilinear2D(float[][][] input, int[] outputSize) {
        int frameSize = input.length;
        int inputHeight = input[0].length;
        int inputWidth = input[0][0].length;
        int outputHeight = outputSize[0];
        int outputWidth = outputSize[1];

        float[][][] output = new float[frameSize][outputHeight][outputWidth];

            for (int c = 0; c < frameSize; c++) {
                for (int oh = 0; oh < outputHeight; oh++) {
                    for (int ow = 0; ow < outputWidth; ow++) {
                        float ih = (float) oh * (inputHeight - 1) / (outputHeight - 1);
                        float iw = (float) ow * (inputWidth - 1) / (outputWidth - 1);

                        int h0 = (int) Math.floor(ih);
                        int w0 = (int) Math.floor(iw);
                        int h1 = Math.min(h0 + 1, inputHeight - 1);
                        int w1 = Math.min(w0 + 1, inputWidth - 1);

                        float dh = ih - h0;
                        float dw = iw - w0;

                        output[c][oh][ow] = lerp(
                                lerp(input[c][h0][w0], input[c][h0][w1], dw),
                                lerp(input[c][h1][w0], input[c][h1][w1], dw),
                                dh
                        );
                    }
                }
            }

        return output;
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

}

