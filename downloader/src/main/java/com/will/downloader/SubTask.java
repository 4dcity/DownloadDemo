package com.will.downloader;

import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubTask implements Runnable {
    private DownloadRecord record;
    @Expose
    private int startLocation;
    @Expose
    private int endLocation;

    private InputStream is;
    private RandomAccessFile file;

    public SubTask(DownloadRecord record, int startLocation, int endLocation) {
        this.record = record;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    void setRecord(DownloadRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(record.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + startLocation + "-" + endLocation);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(DownloadUtil.TIME_OUT);
            conn.setReadTimeout(30 * 1000);
            is = conn.getInputStream();
            file = new RandomAccessFile(record.getFilePath(), "rwd");
            file.seek(startLocation);
            byte[] buffer = new byte[4096];
            int len;

            while (record.getDownloadState() == DownloadUtil.STATE_DOWNLOADING
                    && (len = is.read(buffer)) != -1) {
                file.write(buffer, 0, len);
                startLocation += len;
                record.increaseLength(len);
                DownloadUtil.get().progressUpdated(record);
            }

            if (record.getDownloadState() == DownloadUtil.STATE_DOWNLOADING) {
                if (record.completeSubTask()) {
                    DownloadUtil.get().taskFinished(record);
                }
            }
        } catch (IOException exception) {
            DownloadUtil.get().downloadFailed(record, "subtask failed!");
        } finally {
            try {
                DownloadUtil.get().saveRecord(record);
                file.close();
                is.close();
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}