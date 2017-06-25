package com.courcelle.timelapser.utils;

import java.io.File;

public class FileUtils {
    public static int remoteOldFiles(File path,long olderThan) {
        int count=0;
        if (path.isDirectory()) {
            for(File file: path.listFiles()) {
                if (file.lastModified()<olderThan) {
                    if (file.delete()) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
