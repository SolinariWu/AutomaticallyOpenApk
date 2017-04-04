package com.example.solinari.automaticallyopenapk;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import java.io.File;

/**
 * Created by Solinari on 2017/3/2.
 */

public class DownloadCompleteReceiver extends BroadcastReceiver {
    static SharedPreferencesHelper sp ;
    Context con;
    public DownloadCompleteReceiver(Context context){
        con = context;
        sp = new SharedPreferencesHelper(context);
    }
    @SuppressLint("NewApi")
    public void onReceive(Context context, Intent intent) {
        long downLoadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        long cacheDownLoadId = sp.getDownloadID();
        if (cacheDownLoadId == downLoadId) {
            Intent install = new Intent(Intent.ACTION_VIEW);
            File apkFile = queryDownloadedApk(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//Android 7.0 需要透過FileProvider來取得APK檔的Uri
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
                install.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(install);//開啟安裝畫面
        }
    }

    //透過downLoadId尋找下載的apk檔，解决6.0以上版本安裝問題
    public static File queryDownloadedApk(Context context) {
        File targetApkFile = null;
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        long downloadId =sp.getDownloadID();
        if (downloadId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;
    }
}

