package com.will.downloaddemo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.will.downloaddemo.DownloadUtil.STATE_REENQUEUE;
import static com.will.downloaddemo.DownloadUtil.sDownloadPermit;

/**
 * Created by Will on 2017/3/13.
 */
public class TaskDispatcher extends Thread {
    private BlockingQueue<DownloadRecord> mRecordQueue;
    private volatile boolean mQuit = false;

    public TaskDispatcher() {
        mRecordQueue = new LinkedBlockingQueue<>();
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                DownloadRecord record = mRecordQueue.take();
                sDownloadPermit.acquire();
                if (record.getDownloadState() == STATE_REENQUEUE) {
                    DownloadUtil.get().resume(record.getId());
                } else {
                    DownloadUtil.get().start(record);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mQuit) {
                    return;
                }
            }
        }
    }

    public void enqueueRecord(DownloadRecord record) {
        mRecordQueue.add(record);
    }
}
