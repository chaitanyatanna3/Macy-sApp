package com.example.chaitanya.macysapp;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chaitanya.macysapp.Service.ScanService;

import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm/dd/yyyy hh:mm:ss");
    private LinearLayout linearLayout;
    private Intent intent;
    private ProgressBar progressBar;
    private boolean shareState;
    private StringBuilder dataBuild;
    private TextView textView;
    private AlertDialog alertDialog;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    long startTime;
    long elapsedTime = 0;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getStringExtra("Action");
            switch (action) {
                case "Stop":
                    break;
                case "Progress":
                    int level = intent.getIntExtra("Progress", 0);
                    progressBar.setProgress(level + 1);
                    if (elapsedTime > 10) {
                        builder.setProgress(intent.getIntExtra("ProgressValue", 100), level + 1, false);
                        notificationManager.notify(1, builder.build());
                        startTime = System.currentTimeMillis();
                        elapsedTime = 0;
                    } else  {
                        elapsedTime = new Date().getTime() - startTime;
                    }
                    textView.setText(getString(R.string.scanning) + (level + 1) + "/" + progressBar.getMax());
                    break;
                case "progressInit":
                    linearLayout.removeAllViews();
                    cleanAllResources();
                    builder.setProgress(intent.getIntExtra("ProgressValue", 100), 0, false);
                    notificationManager.notify(1, builder.build());
                    progressBar.setMax(intent.getIntExtra("ProgressValue", 100));
                    shareState = false;
                    invalidateOptionsMenu();
                    break;
                case "Done":
                    textView.setText(R.string.results);
                    break;
                default:
                    textView.setText("");
                    ArrayList<File> tenBiggestFiles = (ArrayList<File>) intent.getSerializableExtra("10BiggestFiles");
                    long averageFileSize = intent.getLongExtra("Average", 0);
                    ArrayList<File> mostRecentFiles = (ArrayList<File>) intent.getSerializableExtra("MostRecent");
                    TextView textView1 = new TextView(context);
                    textView1.setText("10 Biggest Files");
                    dataBuild.append("10 Biggest Files \n");
                    textView1.setTextColor(Color.RED);
                    textView1.setTypeface(Typeface.DEFAULT);
                    linearLayout.addView(textView1);
                    for (int i = 0; i < tenBiggestFiles.size(); i++) {
                        textView1 = new TextView(context);
                        textView1.setTextColor(Color.GRAY);
                        textView1.setTypeface(Typeface.DEFAULT);
                        textView1.setText(tenBiggestFiles.get(i).getName() + " " +  getSizeByKb(tenBiggestFiles.get(i).length()) + "KB");
                        textView1.setPadding(5, 5, 5, 5);
                        linearLayout.addView(textView1);
                    }
                    textView1 = new TextView(context);
                    textView1.setText("Average File Size");
                    dataBuild.append("Averageg File Size \n");
                    textView1.setTextColor(Color.RED);
                    textView1.setTypeface(Typeface.DEFAULT);
                    linearLayout.addView(textView1);
                    textView1 = new TextView(context);
                    textView1.setTextColor(Color.GRAY);
                    textView1.setTypeface(Typeface.DEFAULT);
                    textView1.setText(getSizeByKb(averageFileSize) + "KB");
                    dataBuild.append(getSizeByKb(averageFileSize)).append("KB \n");
                    textView1.setPadding(5, 5, 5, 5);
                    linearLayout.addView(textView1);
                    textView1 = new TextView(context);
                    textView1.setText("Frequent Extensions");
                    dataBuild.append("Average File Size \n");
                    textView1.setTextColor(Color.RED);
                    textView1.setTypeface(Typeface.DEFAULT);
                    linearLayout.addView(textView1);

                    for (int i = 0; i < mostRecentFiles.size(); i++) {
                        textView1 = new TextView(context);
                        textView1.setTextColor(Color.GRAY);
                        textView1.setTypeface(Typeface.DEFAULT);
                        textView.setText(FilenameUtils.getExtension(mostRecentFiles.get(i).getName()) + " Modified Date : " + DATE_FORMAT.format(mostRecentFiles.get(i).lastModified()));
                        dataBuild.append(FilenameUtils.getExtension(mostRecentFiles.get(i).getName())).append(" ").append("Modified Date: ").append(DATE_FORMAT.format(mostRecentFiles.get(i).lastModified())).append("\n");
                        textView1.setPadding(5, 5, 5, 5);
                        linearLayout.addView(textView1);
                    }
                    shareState = true;
                    invalidateOptionsMenu();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        linearLayout = (LinearLayout) findViewById(R.id.result);
        dataBuild = new StringBuilder();
        linearLayout.removeAllViews();
        textView = (TextView) findViewById(R.id.textView);
        progressBar = (ProgressBar) findViewById(R.id.scanning);
        registerReceiver(broadcastReceiver, new IntentFilter("com.chaitanya.macysapp.broadcast"));
        intent = new Intent(MainActivity.this, ScanService.class);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(MainActivity.this);
        builder.setContentIntent(pendingIntent).setContentTitle("Scan").setContentText("Scanning going on").setSmallIcon(R.drawable.ic_scanner_black_24dp);
    }

    private  void cleanAllResources() {
        progressBar.setProgress(0);
        textView.setText(" ");
        shareState = false;
        invalidateOptionsMenu();
        dataBuild = new StringBuilder();
        notificationManager.cancel(1);
    }

    private long getSizeByKb(long size) {
        return size / 1024;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.getItem(0).setVisible(shareState);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.btn_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, dataBuild.toString());
            startActivity(Intent.createChooser(shareIntent, "Share Using"));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        stopService(intent);
    }

    public void stopScan(View view) {
        if (isMyServiceRunning(ScanService.class)) {
            stopService(intent);
        } else {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Scan is Stopped")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.hide();
                        }
                    });
            alertDialog = alertBuilder.create();
            alertDialog.show();
        }
    }

    public void startScan(View view) {
        if (!isMyServiceRunning(ScanService.class)) {
            startService(intent);
        } else {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage("Scan is started")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.hide();
                        }
                    });
            alertDialog = alertBuilder.create();
            alertDialog.show();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

