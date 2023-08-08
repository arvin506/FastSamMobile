package com.example.pytochmobiledemo;

public class SplitArrayUtils {
    public static float[][][] splitArray(float[][][] src, int start1, int len1, int start2, int len2, int start3, int len3) {

        float[][][] result = new float[len1][len2][len3];
        for (int i = start1; i < start1 + len1; i++) {
            for (int j = start2; j < start2 + len2; j++) {
                for (int k = start3; k < start3 + len3; k++) {
                    result[i - start1][j - start2][k - start3] = src[i][j][k];
                }
            }
        }
        return result;
    }

    public static float[][] splitArray(float[][] src, int start1, int len1, int start2, int len2) {
        float[][] result = new float[len1][len2];
        for (int i = start1; i < start1 + len1; i++) {
            for (int j = start2; j < start2 + len2; j++) {
                result[i - start1][j - start2] = src[i][j];

            }
        }
        return result;
    }
}
