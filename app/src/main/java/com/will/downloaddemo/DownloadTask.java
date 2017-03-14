package com.will.downloaddemo;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.TASK_EXECUTOR;
import static com.will.downloaddemo.DownloadUtil.THREAD_NUM;
import static com.will.downloaddemo.DownloadUtil.TIME_OUT;
import static com.will.downloaddemo.DownloadUtil.sPermit;
import static com.will.downloaddemo.DownloadUtil.sRecordMap;

/**
 * Created by Will on 2017/3/14.
 */

public class DownloadTask extends AsyncTask<DownloadRecord, Integer, DownloadRecord> {
    @Override
    protected DownloadRecord doInBackground(DownloadRecord... params) {
        DownloadRecord record = params[0];
        try {
            URL url = new URL(record.getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setConnectTimeout(TIME_OUT);
            conn.connect();
            int fileLength = conn.getContentLength();
            RandomAccessFile file = new RandomAccessFile(record.getFilePath(), "rwd");
            file.setLength(fileLength);
            record.setFileLength(fileLength);
            return record;
        } catch (IOException e) {
            e.printStackTrace();
            sPermit.release();
        }

        return null;
    }

    @Override
    protected void onPostExecute(DownloadRecord downloadRecord) {
        sRecordMap.put(downloadRecord.getId(), downloadRecord);
        int blockSize = downloadRecord.getFileLength() / THREAD_NUM;
        downloadRecord.setDownloadState(STATE_DOWNLOADING);
        for (int i = 0; i < THREAD_NUM; i++) {
            int startL = i * blockSize;
            int endL = (i + 1) * blockSize;
            if (i == THREAD_NUM - 1)
                endL = downloadRecord.getFileLength();
            SubTask subTask = new SubTask(downloadRecord, startL, endL);
            downloadRecord.getSubTaskList().add(subTask);
            TASK_EXECUTOR.execute(subTask);
        }
    }
}
