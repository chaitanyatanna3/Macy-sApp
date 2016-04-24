package com.example.chaitanya.macysapp.Comparators;

import java.io.File;

/**
 * Created by chaitanyatanna on 4/15/16.
 */
public class DataCompare implements Comparable {

    private File file;

    public DataCompare(File file) {
        this.file = file;
    }

    @Override
    public int compareTo(Object another) {
        DataCompare file1 = (DataCompare) another;
        if (file.lastModified() == file1.getFile().lastModified()) {
            return 0;
        } else if (file.lastModified() < file1.getFile().lastModified()) {
            return 1;
        } else {
            return -1;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file){
        this.file =  file;
    }
}
