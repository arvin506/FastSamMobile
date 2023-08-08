package com.example.pytochmobiledemo;

public class PixelsUtils {

    public static float[][] scaleMasks(float[][] inputArray) {
        float[][] outputArray = new float[1024][1024];
        for (int w = 0; w < 256; w++) {
            for (int h = 0; h < 256; h++) {
                float value = inputArray[w][h];
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        outputArray[w * 4 + i][h * 4 + j] = value;
                    }
                }
            }
        }
        return outputArray;
    }
}
