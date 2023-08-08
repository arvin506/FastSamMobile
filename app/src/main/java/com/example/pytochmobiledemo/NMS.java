package com.example.pytochmobiledemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NMS {
    public static int[] nms(float[][] boxes, float[] scores, float iouThreshold) {
        List<int[]> boxScores = new ArrayList<>();
        for (int i = 0; i < boxes.length; i++) {
            float[] box = boxes[i];
            boxScores.add(new int[]{i, (int) (box[0] * 1000), (int) (box[1] * 1000), (int) (box[2] * 1000), (int) (box[3] * 1000)});
        }

        // 根据 scores 降序排序
        Collections.sort(boxScores, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return Float.compare(scores[o2[0]], scores[o1[0]]);
            }
        });

        List<Integer> selectedIndices = new ArrayList<>();
        while (!boxScores.isEmpty()) {
            int[] box = boxScores.get(0);
            selectedIndices.add(box[0]);

            boxScores.remove(0);
            for (int i = boxScores.size() - 1; i >= 0; i--) {
                int[] otherBox = boxScores.get(i);
                float iou = calculateIOU(box, otherBox);
                if (iou > iouThreshold) {
                    boxScores.remove(i);
                }
            }
        }

        // 将 List 转换为 int[]
        int[] result = new int[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            result[i] = selectedIndices.get(i);
        }

        return result;
    }

    private static float calculateIOU(int[] box1, int[] box2) {
        int x1 = Math.max(box1[1], box2[1]);
        int y1 = Math.max(box1[2], box2[2]);
        int x2 = Math.min(box1[3], box2[3]);
        int y2 = Math.min(box1[4], box2[4]);

        int intersectionArea = Math.max(0, x2 - x1 + 1) * Math.max(0, y2 - y1 + 1);
        int box1Area = (box1[3] - box1[1] + 1) * (box1[4] - box1[2] + 1);
        int box2Area = (box2[3] - box2[1] + 1) * (box2[4] - box2[2] + 1);

        return (float) intersectionArea / (box1Area + box2Area - intersectionArea);
    }

}