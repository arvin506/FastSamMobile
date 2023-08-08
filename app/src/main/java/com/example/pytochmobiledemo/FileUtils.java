package com.example.pytochmobiledemo;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.logging.Logger;

public class FileUtils {

    public static void saveFloatArrayToFile(Context context, String fileName, float[] data) {
        PrintWriter writer = null;
        try {
            // Choose the file location (internal or external storage)
            File file = new File(context.getExternalFilesDir(null), fileName);
            // If you want to save to internal storage instead, use:
            // File file = new File(context.getFilesDir(), fileName);

            // Create a PrintWriter to write the float values to the file
            writer = new PrintWriter(new FileWriter(file));

            // Write the float values to the file, one value per line
            for (float value : data) {
                writer.print(value);
                writer.print(" ");
            }

            // File saved successfully
            // Do any additional processing if needed
            Log.e("xiaoyi","file : " +file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error if the file cannot be saved
        } finally {
            // Close the PrintWriter
            if (writer != null) {
                writer.close();
            }
        }
    }
}
