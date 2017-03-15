package com.will.downloaddemo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.will.downloaddemo.DownloadUtil.STATE_CANCELED;
import static com.will.downloaddemo.DownloadUtil.STATE_INITIAL;
import static com.will.downloaddemo.DownloadUtil.STATE_WAITING;
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
                DownloadRecord record = mRecordQueue.take();
                sPermit.acquire();
                if(record.getDownloadState() == STATE_INITIAL
                        || record.getDownloadState() == STATE_CANCELED) {
                    DownloadUtil.get().start(record);
                }else if(record.getDownloadState() == STATE_WAITING){
                    DownloadUtil.get().resume(record.getId());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mQuit) {
                    return;
                }
            }
        }
    }

    public void addRequest(DownloadRequest request) {
        DownloadRecord record = new DownloadRecord(request);
        sRecordMap.put(request.getId(), record);
        mRecordQueue.add(record);
        DownloadUtil.get().newTaskAdd(record);
    }

    public void enqueueRecord(DownloadRecord record){
        mRecordQueue.add(record);
    }
}
