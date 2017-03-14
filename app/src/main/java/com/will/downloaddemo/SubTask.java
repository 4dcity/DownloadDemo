package com.will.downloaddemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.STATE_FAILED;
import static com.will.downloaddemo.DownloadUtil.STATE_FINISHED;
import static com.will.downloaddemo.DownloadUtil.TIME_OUT;

public class SubTask implements Runnable {
    private DownloadRecord record;
    private int startLocation;
    private int endLocation;

    private InputStream is;
    private RandomAccessFile file;

    public SubTask(DownloadRecord record, int startLocation, int endLocation) {
        this.record = record;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(record.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //在头里面请求下载开始位置和结束位置
            conn.setRequestProperty("Range", "bytes=" + startLocation + "-" + endLocation);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(TIME_OUT);
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.setReadTimeout(2000);  //设置读取流的等待时间,必须设置该参数
            is = conn.getInputStream();
            //创建可设置位置的文件
            file = new RandomAccessFile(record.getFilePath(), "rwd");
            //设置每条线程写入文件的位置
            file.seek(startLocation);
            byte[] buffer = new byte[4096];
            int len;
            while (record.getDownloadState() == STATE_DOWNLOADING
                    && (len = is.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                startLocation += len;
                record.increaseLength(len);
                DownloadUtil.get().progressUpdated(record);
            }

            if (record.getDownloadState() == STATE_DOWNLOADING) {
                if (record.completeSubTask()) {
                    record.setDownloadState(STATE_FINISHED);
                    DownloadUtil.get().taskFinished(record);
                }
            }
        } catch (IOException exception) {
            record.setDownloadState(STATE_FAILED);
            DownloadUtil.get().downloadFailed(record,"subtask failed!");
        } finally {
            try {
                file.close();
                is.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}