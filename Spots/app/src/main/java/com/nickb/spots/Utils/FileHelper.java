package com.nickb.spots.Utils;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class FileHelper {

    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/Pictures";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";
    public String STORIES = ROOT_DIR + "/Stories";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";


    public static ArrayList<String> getFilePaths(String directory) {

        ArrayList<String> fileArray = new ArrayList<>();
        java.io.File file = new java.io.File(directory);
        java.io.File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isFile()) {
                fileArray.add(listFiles[i].getAbsolutePath());
            }
        }

        return fileArray;
    }
}
