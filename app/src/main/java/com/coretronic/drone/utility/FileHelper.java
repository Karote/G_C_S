package com.coretronic.drone.utility;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by Morris on 15/8/5.
 */
public class FileHelper {

    String filePath;
    Context context;


    public FileHelper(Context context) {
        this.context = context;
        filePath = context.getExternalFilesDir(null).getAbsolutePath();
        Log.d("morris", "filepath: " + filePath);
    }


    public synchronized void writeToFile(String data, String fileName) {
        try {
            File file = new File(filePath + "/" + fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized String readFromFile(String fileName) {
        String str = null;
        try {
            File file = new File(filePath + "/" + fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            str = bufferedReader.readLine();
        }catch (Exception e){
            e.printStackTrace();
        }

        return str;
    }
}
