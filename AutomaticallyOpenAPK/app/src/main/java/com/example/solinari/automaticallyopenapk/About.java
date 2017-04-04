package com.example.solinari.automaticallyopenapk;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Solinari on 2017/02/15.
 */

public class About extends Navigation_BaseActivity {
    DownloadManager DM ;
    DownloadManager.Request request;
    private DownloadObserver downloadObserver;
    private long LatestDownloadID;
    String URL;
    DialogFragmentHelper newFragment;
    static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        toolbar.setTitle(R.string.about);//設置ToolBar Title
        setUpToolBar();//使用父類別的setUpToolBar()，設置ToolBar
        CurrentMenuItem = 1;//目前Navigation項目位置
        NV.getMenu().getItem(CurrentMenuItem).setChecked(true);//設置Navigation目前項目被選取狀態
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        try {//取得APP目前的versionName
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText( packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadNewVersion();
            }
        });

    }
    private void DownloadNewVersion(){
        newFragment = new DialogFragmentHelper();
        newFragment.show(getSupportFragmentManager(),"download apk");
        DM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        URL = "https://github.com/SolinariWu/AutomaticallyOpenApk/raw/master/Solinari.apk";
        Uri uri = Uri.parse(URL);
        request = new DownloadManager.Request(uri);
        request.setMimeType("application/vnd.android.package-archive");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//Android 6.0以上需要判斷使用者是否願意開啟儲存(WRITE_EXTERNAL_STORAGE)的權限
            CheckStoragePermission();
        }
        else{
            DownloadManagerEnqueue();
        }
    }
    private void DownloadManagerEnqueue(){
        //創建目錄
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
        //設定APK儲存位置
        request.setDestinationInExternalPublicDir(  Environment.DIRECTORY_DOWNLOADS  , "Solinari.apk" ) ;
        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver(getApplicationContext());
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));//註冊DOWNLOAD_COMPLETE-BroadcastReceiver
        downloadObserver = new DownloadObserver(null);
        getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);//註冊ContentObserver
        LatestDownloadID= DM.enqueue(request);
        SharedPreferencesHelper sp = new SharedPreferencesHelper(getApplicationContext());
        sp.setDownloadID(LatestDownloadID);//儲存DownloadID
    }
    private void CheckStoragePermission() {//Android 6.0檢查是否開啟儲存(WRITE_EXTERNAL_STORAGE)的權限，若否，出現詢問視窗
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//Can add more as per requirement
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    20);
        } else {
            DownloadManagerEnqueue();
        }
    }
    @Override//Android 6.0以上 接收使用者是否允許使用儲存權限
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 20: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DownloadManagerEnqueue();
                } else {
                    CheckStoragePermission();
                }
                return;
            }
        }
    }
    class DownloadObserver extends ContentObserver
    {
        public DownloadObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(LatestDownloadID);
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final Cursor cursor = dm.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                final int totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                final int currentColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalSize = cursor.getInt(totalColumn);
                int currentSize = cursor.getInt(currentColumn);
                float percent = (float) currentSize / (float) totalSize;
                final int progress = Math.round(percent * 100);
                runOnUiThread(new Runnable() {//確保在UI Thread執行
                    @Override
                    public void run() {
                        newFragment.setProgress(progress);
                    }
                });
            }
        }
    }
}
