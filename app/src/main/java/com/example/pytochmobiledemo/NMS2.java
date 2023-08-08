package com.example.pytochmobiledemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NMS2 {
    public static int[] nms(float[][] boxes, float[] scores, float iouThreshold) {
        List<float[]> boxScores = new ArrayList<>();
        for (int i = 0; i < boxes.length; i++) {
            float[] box = boxes[i];
            boxScores.add(new float[]{i,  (box[0] * 1000),  (box[1] * 1000),  (box[2] * 1000),  (box[3] * 1000)});
        }



        List<Float> selectedIndices = new ArrayList<>();
        while (!boxScores.isEmpty()) {
            float[] box = boxScores.get(0);
            selectedIndices.add(box[0]);

            boxScores.remove(0);
            for (int i = boxScores.size() - 1; i >= 0; i--) {
                float[] otherBox = boxScores.get(i);
                float iou = calculateIOU(box, otherBox);
                if (iou > iouThreshold) {
                    boxScores.remove(i);
                }
            }
        }

        // 将 List 转换为 int[]
        int[] result = new int[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            result[i] = Math.round(selectedIndices.get(i));
        }

        return result;
    }

    private static float calculateIOU(float[] box1, float[] box2) {
        float x1 = Math.max(box1[1], box2[1]);
        float y1 = Math.max(box1[2], box2[2]);
        float x2 = Math.min(box1[3], box2[3]);
        float y2 = Math.min(box1[4], box2[4]);

        float intersectionArea = Math.max(0, x2 - x1 + 1) * Math.max(0, y2 - y1 + 1);
        float box1Area = (box1[3] - box1[1] + 1) * (box1[4] - box1[2] + 1);
        float box2Area = (box2[3] - box2[1] + 1) * (box2[4] - box2[2] + 1);

        return (float) intersectionArea / (box1Area + box2Area - intersectionArea);
    }

}