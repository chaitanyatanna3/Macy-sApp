package com.example.chaitanya.macysapp.Comparators;

import java.io.File;

/**
 * Created by chaitanyatanna on 4/15/16.
 */
public class SizeCompare implements Comparable {

    private File file;

    public SizeCompare(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(Object another) {
        SizeCompare sizeCompare = (SizeCompare) another;
        if (file.length() == sizeCompare.file.length()) {
            return 0;
        } else if (file.length() < sizeCompare.file.length()) {
            return 1;
        } else {
            return -1;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
