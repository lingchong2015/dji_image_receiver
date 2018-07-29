package com.curry.stephen.djidroneimagereceiver;

import android.content.Context;

import java.io.File;

public class FileHelper {

    public static String getDiskCacheRootDir(Context context) {
        File diskRootFile = context.getCacheDir();

        String cachePath;
        if (diskRootFile != null) {
            cachePath = diskRootFile.getPath();
        } else {
            throw new IllegalArgumentException("disk is invalid");
        }
        return cachePath;
    }
}
