package com.example.pytochmobiledemo;

public class ReshapeUtils {
    /**
     * protos.view(c,-1)
     * @param protos
     * @return
     */
    public static float[][] reshape(float[][][] protos) {
        int channels = protos.length;
        int height = protos[0].length;
        int width = protos[0][0].length;

        // Calculate the total number of elements in a channel
        int totalElementsPerChannel = height * width;

        // Flatten the 3D array into a 1D array
        float[] flattenedArray = new float[channels * totalElementsPerChannel];
        int index = 0;
        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    flattenedArray[index++] = protos[c][h][w];
                }
            }
        }

        // Reshape the 1D array into a 2D array with shape (channels, -1)
        float[][] reshapedArray = new float[channels][];
        index = 0;
        for (int c = 0; c < channels; c++) {
            reshapedArray[c] = new float[totalElementsPerChannel];
            for (int i = 0; i < totalElementsPerChannel; i++) {
                reshapedArray[c][i] = flattenedArray[index++];
            }
        }

        return reshapedArray;
    }


    public static float[][][] reshape(float[][] original, int[] shape) {
        int depth = shape[0];
        int newRows = shape[1];
        int newCols = shape[2];

        float[][][] reshaped = new float[depth][newRows][newCols];

        int row = 0;
        int col = 0;
        int depthIndex = 0;

        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < original[i].length; j++) {
                reshaped[depthIndex][row][col] = original[i][j];
                col++;
                if (col == newCols) {
                    col = 0;
                    row++;
                    if (row == newRows) {
                        row = 0;
                        depthIndex++;
                    }
                }
            }
        }

        return reshaped;
    }

    /**
     * 三维数组转四维数组
     * @param input
     * @return
     */
    public static float[][][][] reshape4D(float[][][] input) {
        int b = 1;
        int c = input.length;
        int h = input[0].length;
        int w = input[0][0].length;
        float[][][][] output = new float[b][c][h][w];
        for (int i = 0; i < b; i++) {
            for (int j = 0; j < c; j++) {
                for (int k = 0; k < h; k++) {
                    for (int l = 0; l < w; l++) {
                        output[i][j][k][l] = input[j][k][l];
                    }
                }
            }
        }
        return output;
    }
}
