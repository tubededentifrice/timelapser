package com.courcelle.timelapser.utils;

import java.io.File;

public class FileUtils {
    public static int removeOldFiles(File path, long olderThan) {
        int count=0;
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files!=null) {
                for(File file: files) {
                    if (file.lastModified()<olderThan) {
                        if (file.delete()) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
