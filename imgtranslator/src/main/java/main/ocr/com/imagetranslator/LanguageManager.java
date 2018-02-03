package main.ocr.com.imagetranslator;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/3.
 */

public class LanguageManager {
    private DownloadManager downloadManager;
    private final String TAG = "LanguageManager";
    private Map<Long, String> downloadTask;
    private Context context;
    private final String DOWNLOAD_PATH = "https://raw.githubusercontent.com/SiKang123/tessdata/master/";

    public LanguageManager(Context context) {
        this.context = context;
        downloadTask = new HashMap<>();
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //注册广播接收者，监听下载状态
        context.registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 下载语言包
     */
    public void downloadLanguage(String language) {
        for (long key : downloadTask.keySet()) {
            if (downloadTask.get(key).equals(language))
                return;
        }
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DOWNLOAD_PATH + language + ".traineddata"));
        request.setAllowedOverRoaming(true);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);

        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS + "/tessdata/", language + ".load");

        //将下载请求加入下载队列
        long mTaskId = downloadManager.enqueue(request);
        downloadTask.put(mTaskId, language);
    }


    /**
     * 下载状态广播
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id != -1)
                checkDownloadStatus(id);//检查下载状态
        }
    };


    /**
     * 检查下载状态
     */
    private void checkDownloadStatus(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.i(TAG, ">>>下载暂停");
                case DownloadManager.STATUS_PENDING:
                    Log.i(TAG, ">>>下载延迟");
                case DownloadManager.STATUS_RUNNING:
                    Log.i(TAG, ">>>正在下载");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Log.i(TAG, ">>>下载完成");
                    if (downloadTask.containsKey(id)) {
                        File file = new File(ImageTranslator.languageDir + downloadTask.get(id) + ".load");
                        if (file.exists()) {
                            renameFile(file.getAbsolutePath(), ImageTranslator.languageDir + downloadTask.get(id) + ".traineddata");
                        }
                    }

                    break;
                case DownloadManager.STATUS_FAILED:
                    Log.i(TAG, ">>>下载失败");
                    if (downloadTask.containsKey(id)) {
                        File file = new File(ImageTranslator.languageDir + downloadTask.get(id) + ".load");
                        if (file.exists())
                            file.delete();
                        downloadTask.remove(id);
                    }
                    break;
            }
        }

    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

    /**
     * 清除没下载完成的文件
     */
    public void clearDownload() {
        File tessdata = new File(ImageTranslator.languageDir);
        if (tessdata.exists() && tessdata.isDirectory()) {
            for (File file : tessdata.listFiles()) {
                String path = file.getAbsolutePath();
                if (path.endsWith(".load")) {
                    file.delete();
                }
            }
        }
    }


}
