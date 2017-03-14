package com.will.downloaddemo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.will.downloaddemo.DownloadUtil.STATE_INITIAL;
import static com.will.downloaddemo.DownloadUtil.TASK_EXECUTOR;
import static com.will.downloaddemo.DownloadUtil.sPermit;
import static com.will.downloaddemo.DownloadUtil.sRecordMap;

/**
 * Created by Will on 2017/3/13.
 */
public class RequestDispatcher extends Thread{
    private BlockingQueue<DownloadRecord> mRecordQueue;
    private volatile boolean mQuit = false;

    public RequestDispatcher() {
        mRecordQueue = new LinkedBlockingQueue<>();
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        while (true){
            try {
                DownloadRecord downloadRecord = mRecordQueue.take();
                sPermit.acquire();
                if(downloadRecord.getDownloadState() == STATE_INITIAL) {
                    new DownloadTask().executeOnExecutor(TASK_EXECUTOR, downloadRecord);
                }else{
                    downloadRecord.resumeDownload();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mQuit) {
                    return;
                }
                continue;
            }
        }
    }

    public void addRequest(DownloadRequest request) {
        DownloadRecord record = new DownloadRecord(request);
        sRecordMap.put(request.getId(), record);
        mRecordQueue.add(record);

    }

    public void enqueueRecord(DownloadRecord record){
        mRecordQueue.add(record);
    }
}
