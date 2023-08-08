package com.example.pytochmobiledemo;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;

import java.util.*;

public class Utils {
    public static List<Object> nonMaxSuppression(float[] prediction, float[] proto) {
        //转成tensor
        float[][][] tensor = convertToTensor3D(prediction, new int[]{1, 37, 21504});
        //转置维度 [1,37,21504] -> [21504,37,1]
        tensor = transposeTensor(tensor);

        //置信度阈值过滤 数组中为1的表示置信度 > 0.4
        float[] filterConfThres = filterConfThres(tensor);
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < filterConfThres.length; i++) {
            if (filterConfThres[i] > 0) {
                indexs.add(i);
            }
        }

        //python 代码中的 x
        float[][][] confTres3D = new float[indexs.size()][37][];
        for (int i = 0; i < indexs.size(); i++) {
            confTres3D[i] = tensor[indexs.get(i)];
        }
        float[][] confTres2D = convertToTwoDimArray(confTres3D);

        //分割 box， cls， mask

        float[][] box = splitArray(confTres2D, 0, 4);
        float[][] cls = splitArray(confTres2D, 4, 1);
        float[][] mask = splitArray(confTres2D, 5, 32);

        box = xywh2xyxy(box);

        //取最大值 对应 conf, j = cls.max(1, keepdim=True)
        float[][] conf = new float[cls.length][1];
        int[][] j = new int[cls.length][1];
        max(cls, conf, j);
        float[][] jFloat = toFloat(j);
        //x = torch.cat((box, conf, j.float(), mask), 1)
        float[][] catArray = cat(box, conf, jFloat, mask);
        //[conf.view(-1) > conf_thres]
        float[] confReshape = reshape(conf);

        List<float[]> xList = new ArrayList<>();
        for (int i = 0; i < confReshape.length; i++) {
            // 判断对应位置上的 conf 是否大于阈值
            if (confReshape[i] > 0.4) {
                // 如果满足条件，则将对应位置上的 a 元素添加到列表中
                xList.add(catArray[i]);
            }
        }

        // 将列表转换为二维数组
        float[][] x = new float[xList.size()][];
        for (int i = 0; i < xList.size(); i++) {
            x[i] = xList.get(i);
        }
        //x = x[x[:, 4].argsort(descending=True)[:max_nms]]
        x = sort(x);
        // c = x[:, 5:6] * (0 if agnostic else max_wh)
        float[][] c = splitArray(x, 5, 1);

        //boxes, scores = x[:, :4] + c, x[:, 4]
        float[][] boxs = splitArray(x, 0, 4);
        float[][] scores = splitArray(x, 4, 1);
        float[] scoresReshape = reshape(scores);


        int[] i = NMS2.nms(boxs, scoresReshape, 0.9f);
        //output 对应pred
        Log.e("xiaoyi","box size : " + i.length);
        float[][] output = getNmsBoxs(x, i,33);
//        float[][] fullBox = createFullBox(output, new int[]{1, 3, 1024, 1024});
//        int[] critical_iou_index = BoundingBoxIoU.bbox_iou(splitArray(fullBox, 0, 4),
//                splitArray(output, 0, 4),
//                0.9f,
//                new int[]{1024, 1024},
//                false
//        );

        float[][][][] tensorProto = convertToTensor4D(proto, new int[]{1, 32, 256, 256});

        //pred[:,:4]
        float[][] pred = splitArray(output, 0, 4);
        //pred[:, :4] = ops.scale_boxes(img.shape[2:], pred[:, :4], orig_img.shape)
        pred = BoxScaler.scaleBoxes(new int[]{1024, 1024}, pred, new int[]{1024, 1024, 3}, null);
        //pred[:, 6:]
        float[][] maskIn = splitArray(output, 6, 32);

        //输出 masks->masks
        //boxes ->boxes
        float[][][] masks = MaskProcessor.processMaskNative(tensorProto[0], maskIn, pred, new int[]{1024, 1024},true);
        float[][] boxes = splitArray(output, 0, 6);

        List<Object> res = new ArrayList<>();
        res.add(masks);
        res.add(boxes);
        return res;

    }


    // 将一维 float[] 转换为三维张量
    public static float[][][] convertToTensor3D(float[] inputArray, int[] shape) {
        int channels = shape[0];
        int height = shape[1];
        int width = shape[2];

        float[][][] tensor = new float[channels][height][width];

        int index = 0;
        for (int c = 0; c < channels; c++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    tensor[c][h][w] = inputArray[index];
                    index++;
                }
            }
        }

        return tensor;
    }

    public static float[][][][] convertToTensor4D(float[] inputArray, int[] shape) {
        int batchs = shape[0];
        int channels = shape[1];
        int height = shape[2];
        int width = shape[3];

        float[][][][] tensor = new float[batchs][channels][height][width];
        int index = 0;
        for (int b = 0; b < batchs; b++) {
            for (int c = 0; c < channels; c++) {
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        tensor[b][c][h][w] = inputArray[index];
                        index++;
                    }
                }
            }
        }
        return tensor;
    }

    /**
     * 三维转二维
     *
     * @param inputArray
     * @param shape
     * @return
     */
    public static float[][] convertToTwoDimArray(float[][][] threeDimArray) {
        int rows = threeDimArray.length;
        int cols = threeDimArray[0].length;
        float[][] twoDimArray = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                twoDimArray[i][j] = threeDimArray[i][j][0];
            }
        }

        return twoDimArray;
    }


    // 进行维度交换
    public static float[][][] transposeTensor(float[][][] tensor) {
        int channels = tensor.length;
        int height = tensor[0].length;
        int width = tensor[0][0].length;

        float[][][] transposedTensor = new float[width][height][channels];

        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                for (int c = 0; c < channels; c++) {
                    transposedTensor[w][h][c] = tensor[c][h][w];
                }
            }
        }

        return transposedTensor;
    }


    /**
     * 过滤置信度低于阈值的
     */
    public static float[] filterConfThres(float[][][] prediction) {
        Log.e("xiaoyi", "start ");
        int startIdx = 4;
        int endIdx = 5;

        float[] res = new float[21504];
        for (int i = startIdx; i < endIdx; i++) {
            for (int j = 0; j < 21504; j++) {
                if (prediction[j][i][0] > 0.4) {
                    res[j] = 1;
                } else {
                    res[j] = 0;
                }
            }
        }

        return res;
    }

    public static float[][] splitArray(float[][] src, int start, int len) {
        int m = src.length;
        int n = src[0].length;
        float[][] result = new float[m][len];
        for (int i = 0; i < m; i++) {
            for (int j = start; j < start + len; j++) {
                result[i][j - start] = src[i][j];
            }
        }
        return result;
    }


    public static float[][] xywh2xyxy(float[][] x) {
        float[][] y = new float[x.length][4];
        for (int i = 0; i < x.length; i++) {
            y[i][0] = x[i][0] - x[i][2] / 2; // top left x
            y[i][1] = x[i][1] - x[i][3] / 2; // top left y
            y[i][2] = x[i][0] + x[i][2] / 2; // bottom right x
            y[i][3] = x[i][1] + x[i][3] / 2; // bottom right y
        }
        return y;
    }

    public static void max(float[][] input, float[][] conf, int[][] index) {
        int m = input.length;
        int n = input[0].length;
        for (int i = 0; i < m; i++) {
            float tmp = 0;
            int indexTmp = 0;
            for (int j = 0; j < n; j++) {
                if (input[i][j] > tmp) {
                    tmp = input[i][j];
                    conf[i][0] = tmp;
                    index[i][0] = indexTmp;
                }
            }
        }
    }

    public static float[][] toFloat(int[][] input) {
        int m = input.length;
        int n = input[0].length;
        float[][] output = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                output[i][j] = input[i][j] * 1.0f;
            }
        }
        return output;
    }

    /**
     * 拼接box conf j mask 为一个数组
     */
    public static float[][] cat(float[][] box, float[][] conf, float[][] j, float[][] mask) {
        int m = box.length;
        int n = box[0].length + conf[0].length + j[0].length + mask[0].length;
        float[][] output = new float[m][n];
        for (int i = 0; i < m; i++) {
            int index = 0;
            for (int boxj = 0; boxj < box[0].length; boxj++) {
                output[i][index] = box[i][boxj];
                index++;
            }
            for (int confJ = 0; confJ < conf[0].length; confJ++) {
                output[i][index] = conf[i][confJ];
                index++;
            }
            for (int jj = 0; jj < j[0].length; jj++) {
                output[i][index] = j[i][jj];
                index++;
            }
            for (int maskJ = 0; maskJ < mask[0].length; maskJ++) {
                output[i][index] = mask[i][maskJ];
                index++;
            }
        }
        return output;
    }

    public static float[] reshape(float[][] input) {
        int m = input.length;
        int n = input[0].length;
        float[] output = new float[m * n];
        int index = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                output[index] = input[i][j];
                index++;
            }
        }
        return output;
    }


    public static float[][] sort(float[][] input) {

        int m = input.length;
        int n = input[0].length;

        // 获取 x 数组的第 4 列元素所在的一维数组
        float[] column4 = new float[m];
        for (int i = 0; i < m; i++) {
            column4[i] = input[i][4];
        }

        // 对第 4 列元素进行降序排序，并获取排序后的索引数组
        Integer[] sortedIndices = new Integer[m];
        for (int i = 0; i < m; i++) {
            sortedIndices[i] = i;
        }

        Arrays.sort(sortedIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer index1, Integer index2) {
                return Float.compare(column4[index2], column4[index1]);
            }
        });

        // 取排序后的前 max_det 个索引

        // 根据 topIndices 获取排序后的结果数组
        float[][] result = new float[m][n];
        for (int i = 0; i < m; i++) {
            int rowIndex = sortedIndices[i];
            result[i] = input[rowIndex];
        }


        return result;
    }

    public static float[][] getNmsBoxs(float[][] input, int[] indexs, int maxDet) {
        int m = Math.min(maxDet, indexs.length);
        int n = input[0].length;
        float[][] result = new float[m][n];
        for (int i = 0; i < m; i++) {
            result[i] = input[indexs[i]];
        }
        return result;
    }

    public static float[][] createFullBox(float[][] input, int[] shape) {
        int m = input[0].length;
        float[][] result = new float[1][m];
        result[0][2] = shape[3];
        result[0][3] = shape[2];
        result[0][4] = 1;
        for (int i = 6; i < m; i++) {
            result[0][i] = 1;
        }
        return result;
    }

    public static float[][][] cropMask(float[][][] masks, float[][] boxes) {
        int n = masks.length;
        int h = masks[0].length;
        int w = masks[0][0].length;
        float[][] x1 = SplitArrayUtils.splitArray(boxes, 0, n, 0, 1);
        float[][] y1 = SplitArrayUtils.splitArray(boxes, 0, n, 1, 1);
        float[][] x2 = SplitArrayUtils.splitArray(boxes, 0, n, 2, 1);
        float[][] y2 = SplitArrayUtils.splitArray(boxes, 0, n, 3, 1);

        float[] r = new float[w];
        for (int i = 0; i < r.length; i++) {
            r[i] = i;
        }
        float[] c = new float[h];
        for (int i = 0; i < c.length; i++) {
            c[i] = i;
        }

        float[][][] __r = new float[1][1][r.length];
        for (int i = 0; i < r.length; i++) {
            __r[0][0][i] = r[i];
        }
        float[][][] _c_ = new float[1][c.length][1];
        for (int i = 0; i < c.length; i++) {
            _c_[0][i][0] = c[i];
        }
        float[][][] result = new float[n][h][w];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < h; j++) {
                for (int k = 0; k < w; k++) {
                    //改成c的索引为j去判断
//                    boolean condition = (__r[0][0][k] >= x1[i][0]) && (__r[0][0][k] < x2[i][0]) && (_c_[0][k][0] >= y1[i][0]) && (_c_[0][k][0] < y2[i][0]);
                    boolean condition = (__r[0][0][k] >= x1[i][0]) && (__r[0][0][k] < x2[i][0]) && (_c_[0][j][0] >= y1[i][0]) && (_c_[0][j][0] < y2[i][0]);
                    if (condition) {
                        result[i][j][k] = masks[i][j][k];
                    } else {
                        result[i][j][k] = 0;
                    }
                }
            }
        }
        return result;
    }

    public static double[][][] cropMask2(double[][][] masks, double[][] boxes) {
        int n = masks.length;
        int h = masks[0].length;
        int w = masks[0][0].length;

        double[][][] croppedMasks = new double[n][h][w];

        for (int i = 0; i < n; i++) {
            double x1 = boxes[i][0];
            double y1 = boxes[i][1];
            double x2 = boxes[i][2];
            double y2 = boxes[i][3];

            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    if (Math.abs(col - x1) < 1e-6 && Math.abs(row - y1) < 1e-6
                            && Math.abs(col - x2) < 1e-6 && Math.abs(row - y2) < 1e-6) {
                        croppedMasks[i][row][col] = masks[i][row][col];
                    }
                }
            }
        }

        return croppedMasks;
    }

    public static double[][][] toDouble(float[][][] input) {
        double[][][] result = new double[input.length][input[0].length][input[0][0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                for (int k = 0; k < input[0][0].length; k++) {
                    result[i][j][k] = input[i][j][k];
                }
            }
        }
        return result;
    }

    public static double[][] toDouble(float[][] input) {
        double[][] result = new double[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                result[i][j] = input[i][j];
            }
        }
        return result;
    }


    public static float[][][] gt(float[][][] input, float threshold) {
        int depth = input.length;
        int rows = input[0].length;
        int cols = input[0][0].length;
        float[][][] result = new float[depth][rows][cols];

        for (int d = 0; d < depth; d++) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (input[d][i][j] > threshold) {
                        result[d][i][j] = 1.0f;
                    } else {
                        result[d][i][j] = 0.0f;
                    }
                }
            }
        }

        return result;
    }

    public static int randomColor(float alpha) {
        Random random = new Random();
        return Color.argb((int) (255 * alpha), random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}
