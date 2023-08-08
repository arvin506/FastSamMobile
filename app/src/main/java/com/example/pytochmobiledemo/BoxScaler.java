package com.example.pytochmobiledemo;

public class BoxScaler {
    public static float[][] scaleBoxes(int[] img1Shape, float[][] boxes, int[] img0Shape, float[] ratioPad) {
        float gain;
        float[] pad;

        if (ratioPad == null) {
            gain = Math.min((float) img1Shape[0] / img0Shape[0], (float) img1Shape[1] / img0Shape[1]);
            pad = new float[]{
                    Math.round((img1Shape[1] - img0Shape[1] * gain) / 2 - 0.1f),
                    Math.round((img1Shape[0] - img0Shape[0] * gain) / 2 - 0.1f)
            };
        } else {
            gain = ratioPad[0];
            pad = new float[]{ratioPad[1], ratioPad[2]};
        }

        for (int i = 0; i < boxes.length; i++) {
            boxes[i][0] -= pad[0]; // x1
            boxes[i][1] -= pad[1]; // y1
            boxes[i][2] -= pad[0]; // x2
            boxes[i][3] -= pad[1]; // y2
            boxes[i][0] /= gain; // x1
            boxes[i][1] /= gain; // y1
            boxes[i][2] /= gain; // x2
            boxes[i][3] /= gain; // y2
        }

        clipBoxes(boxes, img0Shape);
        return boxes;
    }

    private static void clipBoxes(float[][] boxes, int[] imgShape) {
        for (int i = 0; i < boxes.length; i++) {
            boxes[i][0] = Math.max(0, Math.min(boxes[i][0], imgShape[1] - 1)); // x1
            boxes[i][1] = Math.max(0, Math.min(boxes[i][1], imgShape[0] - 1)); // y1
            boxes[i][2] = Math.max(0, Math.min(boxes[i][2], imgShape[1] - 1)); // x2
            boxes[i][3] = Math.max(0, Math.min(boxes[i][3], imgShape[0] - 1)); // y2
        }
    }



}

