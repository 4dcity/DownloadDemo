package com.will.downloaddemo;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;

import static com.will.downloaddemo.DownloadUtil.MSG_FINISHED;
import static com.will.downloaddemo.DownloadUtil.MSG_PROGRESS;
import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.THREAD_NUM;
import static com.will.downloaddemo.DownloadUtil.TIME_OUT;

public class DownloadTask implements Runnable {

    private DownloadRecord downloadRecord;
    private Handler mHandler;
    private ExecutorService mExecutor;

    private DownloadListener listener;

    public DownloadTask(ExecutorService executor, final DownloadRecord record, final DownloadListener listener) {
        downloadRecord = record;
        this.listener = listener;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_FINISHED:
                        listener.onSuccess();
                        break;
                    case MSG_PROGRESS:
                        listener.onProgress(Math.round(downloadRecord.getCurrentLength() /
                                (downloadRecord.getFileLength() * 1.0f) * 100));
                        break;
                }
            }
        };

        mExecutor = executor;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(downloadRecord.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(TIME_OUT);
            //conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            //conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.connect();
            downloadRecord.setFileLength(conn.getContentLength());
            RandomAccessFile file = new RandomAccessFile(downloadRecord.getSaveDir(), "rwd");
            file.setLength(downloadRecord.getFileLength());
            long blockSize = downloadRecord.getFileLength() / THREAD_NUM;
            downloadRecord.setDownloadState(STATE_DOWNLOADING);
            for (int i = 0; i < THREAD_NUM; i++) {
                long startL = i * blockSize;
                long endL = (i + 1) * blockSize;
                if (i == THREAD_NUM - 1)
                    endL = downloadRecord.getFileLength();
                SubTask subTask = new SubTask(downloadRecord, startL, endL);
                downloadRecord.getSubTaskList().add(subTask);
                mExecutor.execute(subTask);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subtaskComplete() {
        if (downloadRecord.completeSubTask()) {
            mHandler.sendEmptyMessage(MSG_FINISHED);
        }
    }

    public void increaseLength(int length) {
        downloadRecord.increaseLength(length);
        mHandler.sendEmptyMessage(MSG_PROGRESS);
    }

    public void resumeDownload() {
        downloadRecord.setDownloadState(STATE_DOWNLOADING);
        for (int i = 0; i < THREAD_NUM; i++) {
            DownloadTask.SubTask subTask = downloadRecord.getSubTaskList().get(i);
            mExecutor.execute(subTask);
        }
    }


    /**
     * 每个下载任务分成多个子任务下载
     */
    class SubTask implements Runnable {
        DownloadRecord downloadRecord;
        long startLocation;
        long endLocation;

        public SubTask(DownloadRecord task, long startLocation, long endLocation) {
            super();
            this.downloadRecord = task;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(downloadRecord.getDownloadUrl());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //在头里面请求下载开始位置和结束位置
                conn.setRequestProperty("Range", "bytes=" + startLocation + "-" + endLocation);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setConnectTimeout(TIME_OUT);
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                conn.setReadTimeout(2000);  //设置读取流的等待时间,必须设置该参数
                InputStream is = conn.getInputStream();
                //创建可设置位置的文件
                RandomAccessFile file = new RandomAccessFile(downloadRecord.getFilePath(), "rwd");
                //设置每条线程写入文件的位置
                file.seek(startLocation);
                byte[] buffer = new byte[4096];
                int len;
                while (downloadRecord.getDownloadState() == STATE_DOWNLOADING
                        && (len = is.read(buffer)) != -1) {
                    file.write(buffer, 0, len);
                    startLocation += len;
                    downloadRecord.increaseLength(len);
                }

                if (downloadRecord.getDownloadState() == STATE_DOWNLOADING) {
                    if (downloadRecord.completeSubTask()) {
                        mHandler.sendEmptyMessage(MSG_FINISHED);
                    }
                }

                file.close();
                is.close();
            } catch (IOException exception) {

            }
        }
    }
}