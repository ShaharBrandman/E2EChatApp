package com.example.e2echatapp.api;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class fileSystem {

    //write to a file method
    public static void writeToFile(Context context, String fileName, String data) {
        //creating a file out stream of the fileName and writing the data into it
        try {
            FileOutputStream out = new FileOutputStream(context.getFilesDir() + "/" + fileName);

            out.write(data.getBytes());
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //deleting a file
    public static void deleteFile(Context context, String fileName) {
        //creating a new file object and deleting it
        new File(context.getFilesDir() + "/" + fileName).delete();
    }

    //get all data stored in a file
    public static String getDataFromFile(Context context, String fileName) {
        //create a string object for the data in the file
        String str = "";

        try {
            File file = new File(context.getFilesDir() + "/" + fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            //get size of the file
            int length = (int) file.length();

            //declare a byte array at the length of the file size
            byte[] bytes = new byte[length];

            //create file input stream of the file
            FileInputStream in = new FileInputStream(file);

            try {
                //write the data to the byte array
                in.read(bytes);
            } finally{
                in.close();
            }

            //convert the byte array to string
            str = new String(bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }
}
