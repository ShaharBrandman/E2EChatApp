package com.example.e2echatapp.api;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileSystem {
    public static void writeToFile(Context context, String fileName, String data) {
        try {
            FileOutputStream out = new FileOutputStream(context.getFilesDir() + "/" + fileName);

            out.write(data.getBytes());
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(Context context, String fileName) {
        new File(context.getFilesDir() + "/" + fileName).delete();
    }

    public static String getDataFromFile(Context context, String fileName) {
        String str = "";

        try {
            File file = new File(context.getFilesDir() + "/" + fileName);

            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally{
                in.close();
            }

            str = new String(bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }
}
