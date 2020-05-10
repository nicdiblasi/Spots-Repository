package com.nickb.spots.Utils;

import android.Manifest;


// class to help check permissions
public class Permissions {

    public static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    public static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;


    public static final String[] WRITE_STORAGE_PERMISSION = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
}
