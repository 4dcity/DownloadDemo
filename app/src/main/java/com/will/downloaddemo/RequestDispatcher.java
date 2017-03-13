package com.will.downloaddemo;

import java.util.concurrent.BlockingQueue;

import static com.will.downloaddemo.DownloadUtil.TASK_EXECUTOR;

/**
 * Created by Will on 2017/3/13.
 */
public class RequestDispatcher extends Thread{
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private volatile boolean mQuit = false;

    public RequestDispatcher(BlockingQueue<DownloadRequest> requestQueue) {
        mRequestQueue = requestQueue;
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }

    @Override
    public void run() {
        while (true){
            try {
                DownloadRequest downloadRequest = mRequestQueue.take();
                TASK_EXECUTOR.execute(new DownloadTask(TASK_EXECUTOR, downloadRequest));
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (mQuit) {
                    return;
                }
                continue;
            }
        }
    }
}
