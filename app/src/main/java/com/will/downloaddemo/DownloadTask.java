package com.will.downloaddemo;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadTask extends Thread{

    private static final int DOWNLOAD_FINISHED = 1;
    private static final int DOWNLOAD_PROGRESS = 2;
    private static final int DOWNLOAD_CANCELED = 3;
    private static final int DOWNLOAD_STOPED = 4;
    private static final int DOWNLOAD_FAILED = 5;

    private final static int TIME_OUT = 5000;
    private final static int THREAD_NUM = 5;
    private Handler mHandler;
    private Executor mExecutor;

    private long currentLocation;
    private long fileLength;
    private String downloadUrl;
    private String filePath;
    private int completedBlock;
    private boolean isCanceled;
    private boolean isPaused;
    private boolean isCompleted;
    private boolean isFailed;
    private DownloadListener listener;

    public DownloadTask(String downloadUrl, String filePath, final DownloadListener listener){
        this.downloadUrl = downloadUrl;
        this.filePath = filePath;
        this.listener = listener;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case DOWNLOAD_FINISHED:
                        listener.onSuccess();
                        break;
                    case DOWNLOAD_PROGRESS:
                        listener.onProgress(Math.round(currentLocation / (fileLength*1.0f) * 100));
                        break;
                }
            }
        };

        mExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.connect();
            fileLength = conn.getContentLength();
            RandomAccessFile file = new RandomAccessFile(filePath, "rwd");
            file.setLength(fileLength);
            long blockSize = fileLength / THREAD_NUM;
            for (int i = 0; i < THREAD_NUM; i++) {
                long startL = i * blockSize;
                long endL = (i + 1) * blockSize;
                if(i == THREAD_NUM - 1)
                    endL = fileLength;
                mExecutor.execute(new DownloadBlock(this, startL, endL));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getCurrentLocation() {
        return currentLocation;
    }

    public long getFileLength() {
        return fileLength;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public void blockComplete(){
        completedBlock++;
        if(completedBlock == THREAD_NUM){
            mHandler.sendEmptyMessage(DOWNLOAD_FINISHED);
        }
    }

    public void increaseLength(int length){
        currentLocation += length;
        mHandler.sendEmptyMessage(DOWNLOAD_PROGRESS);
    }


    class DownloadBlock implements Runnable{
        DownloadTask task;
        long startLocation;
        long endLocation;

        public DownloadBlock(DownloadTask task, long startLocation, long endLocation) {
            super();
            this.task = task;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(task.getDownloadUrl());
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
                RandomAccessFile file = new RandomAccessFile(task.getFilePath(), "rwd");
                //设置每条线程写入文件的位置
                file.seek(startLocation);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    //把下载数据数据写入文件
                    file.write(buffer, 0, len);
                    synchronized (task) {
                        task.increaseLength(len);
                    }
                }

                synchronized (task){
                    task.blockComplete();
                }

                file.close();
                is.close();
            } catch (IOException ioExeception) {

            }
        }
    }
}