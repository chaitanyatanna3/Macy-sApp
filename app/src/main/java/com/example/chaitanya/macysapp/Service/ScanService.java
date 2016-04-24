package com.example.chaitanya.macysapp.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;

import com.example.chaitanya.macysapp.Comparators.DataCompare;
import com.example.chaitanya.macysapp.Comparators.SizeCompare;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by chaitanyatanna on 4/15/16.
 */
public class ScanService extends IntentService {

    private LinkedList<File> fileLinkedList;
    private static final File Root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    private static Intent pIntent;
    private static boolean sStatus;
    private static long totalLength;
    private static long averageLength;
    private ArrayList<SizeCompare> biggestFile;
    private ArrayList<DataCompare> mostRecent;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ScanService(String name) {
        super("File Scan");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        biggestFile = new ArrayList<>();
        mostRecent = new ArrayList<>();
        fileLinkedList = new LinkedList<>();
        pIntent = new Intent();
        totalLength = 0;
        averageLength = 0;
        sStatus = true;
        pIntent.putExtra("Action", "progressInit");
        pIntent.setAction("com.chaitanya.macysapp.broadcast");
        fileLinkedList = (LinkedList<File>) FileUtils.listFiles(Root, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);


    }

    @Override
    protected void onHandleIntent(Intent intent) {

        pIntent.putExtra("ProgressValue", fileLinkedList.size());
        sendBroadcast(pIntent);
        pIntent.putExtra("Action", "Progress");
        setTotalFileLength();
        if (sStatus) {
            averageLength = this.getAverageSize();
            Collections.sort(this.biggestFile);
            Collections.sort(this.mostRecent);
            pIntent.putExtra("Average", averageLength);
            pIntent.putExtra("10BiggestFiles", this.getTenBiggestFiles());
            pIntent.putExtra("MostRecent", this.getMostRecentExtention());
            pIntent.putExtra("Action", "Publish");
            sendBroadcast(pIntent);
        }

    }

    private void setTotalFileLength() {
        for (int i = 0; i < fileLinkedList.size(); i++) {
            if (sStatus) {
                totalLength += fileLinkedList.get(i).length();
                biggestFile.add(new SizeCompare(fileLinkedList.get(i)));
                mostRecent.add(new DataCompare(fileLinkedList.get(i)));
                pIntent.putExtra("Progress", i);
                sendBroadcast(pIntent);
            } else {
                break;
            }
        }
        if (sStatus) {
            pIntent.putExtra("Action", "Done");
            sendBroadcast(pIntent);
        }
    }

    private Long getAverageSize() {
        return totalLength / fileLinkedList.size();
    }

    private ArrayList<File> getTenBiggestFiles() {
        ArrayList<File> tenBiggestFiles = new ArrayList<>();
        try {
            for (int i = 0; i < 10; i++) {
                tenBiggestFiles.add(this.biggestFile.get(i).getFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tenBiggestFiles;
    }

    private ArrayList<File> getMostRecentExtention() {
        ArrayList<File> fiveMostRecent = new ArrayList<>();
        try {
            for (int i = 0; i < 5; i++) {
                fiveMostRecent.add(this.mostRecent.get(i).getFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fiveMostRecent;
    }

    @Override
    public void onDestroy() {
        sStatus = false;
        pIntent.putExtra("Action", "Stop");
        sendBroadcast(pIntent);
    }
}
