package com.will.downloaddemo;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.will.downloaddemo.DownloadUtil.TASK_EXECUTOR;
import static com.will.downloaddemo.DownloadUtil.sThreadNum;
import static com.will.downloaddemo.DownloadUtil.TIME_OUT;

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
            DownloadUtil.get().fileLengthSet(record);
            return record;
        } catch (IOException e) {
            DownloadUtil.get().downloadFailed(record, "Get filelength failed!");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(DownloadRecord record) {
        if (record != null) {
            int blockSize = record.getFileLength() / sThreadNum;
            for (int i = 0; i < sThreadNum; i++) {
                int startL = i * blockSize;
                int endL = (i + 1) * blockSize;
                if (i == sThreadNum - 1)
                    endL = record.getFileLength();
                SubTask subTask = new SubTask(record, startL, endL);
                record.getSubTaskList().add(subTask);
                TASK_EXECUTOR.execute(subTask);
            }
        }
    }
}
