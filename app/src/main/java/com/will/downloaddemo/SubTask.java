package com.will.downloaddemo;

import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.TIME_OUT;

public class SubTask implements Runnable {
    private DownloadRecord record;
    @Expose private int startLocation;
    @Expose private int endLocation;

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
            conn.setReadTimeout(10*1000);  //设置读取流的等待时间,必须设置该参数
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
                    DownloadUtil.get().taskFinished(record);
                }
            }
        } catch (IOException exception) {
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