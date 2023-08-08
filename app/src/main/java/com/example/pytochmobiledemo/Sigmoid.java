package com.example.pytochmobiledemo;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class Sigmoid {

    public static float[][] out(float[][] x) {
        int m = x.length;
        int n = x[0].length;
        float[][] out = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                out[i][j] = sigmoidF(x[i][j]);

            }
        }
        return out;
    }

    private static float sigmoidF(float x) {
//        BigDecimal bigDecimal = new BigDecimal(1d / (1d + Math.pow(Math.E, -1 * x)));
//        return Double.valueOf(bigDecimal.setScale(5, RoundingMode.UP).doubleValue()).floatValue();
        return Double.valueOf((1f / (1f + Math.pow(Math.E, -1 * x)))).floatValue();
    }
}
