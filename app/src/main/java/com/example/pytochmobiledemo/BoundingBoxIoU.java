package com.example.pytochmobiledemo;

public class BoundingBoxIoU {
    public static int[] bbox_iou(float[][] box1, float[][] boxes, float iouThres, int[] imageShape, boolean rawOutput) {
        boxes = adjustBBoxesToImageBorder(boxes, imageShape);

        float[][] intersections = new float[box1.length][boxes.length];
        float[] box1Areas = new float[box1.length];
        float[] box2Areas = new float[boxes.length];
        float[] unions = new float[box1.length];

        for (int i = 0; i < box1.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                float x1 = Math.max(box1[i][0], boxes[j][0]);
                float y1 = Math.max(box1[i][1], boxes[j][1]);
                float x2 = Math.min(box1[i][2], boxes[j][2]);
                float y2 = Math.min(box1[i][3], boxes[j][3]);

                intersections[i][j] = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
                box1Areas[i] = (box1[i][2] - box1[i][0]) * (box1[i][3] - box1[i][1]);
                box2Areas[j] = (boxes[j][2] - boxes[j][0]) * (boxes[j][3] - boxes[j][1]);
                unions[i] = box1Areas[i] + box2Areas[j] - intersections[i][j];
            }
        }

        int[] results = new int[box1.length];
        for (int i = 0; i < box1.length; i++) {
            float iou = intersections[i][0] / unions[i];
            if (rawOutput) {
                results[i] = iou > 0 ? 1 : 0;
            } else {
                results[i] = iou > iouThres ? 1 : 0;
            }
        }

        return results;
    }

    private static float[][] adjustBBoxesToImageBorder(float[][] boxes, int[] imageShape) {
        float[][] adjustedBoxes = new float[boxes.length][4];
        for (int i = 0; i < boxes.length; i++) {
            adjustedBoxes[i][0] = Math.max(0, boxes[i][0]);
            adjustedBoxes[i][1] = Math.max(0, boxes[i][1]);
            adjustedBoxes[i][2] = Math.min(imageShape[0], boxes[i][2]);
            adjustedBoxes[i][3] = Math.min(imageShape[1], boxes[i][3]);
        }
        return adjustedBoxes;
    }

}
