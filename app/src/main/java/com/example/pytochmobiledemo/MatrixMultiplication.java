package com.example.pytochmobiledemo;

public class MatrixMultiplication {
    public static float[][] matrixMultiplication(float[][] masksIn, float[][] protos) {
        int n = masksIn.length;
        int maskDim = masksIn[0].length;
        int maskHMaskW = protos[0].length;

        float[][] result = new float[n][maskHMaskW];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < maskHMaskW; j++) {
                for (int k = 0; k < maskDim; k++) {
                    result[i][j] += masksIn[i][k] * protos[k][j];
                }
            }
        }

        return result;
    }
}
