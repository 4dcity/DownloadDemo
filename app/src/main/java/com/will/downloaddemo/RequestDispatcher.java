package com.will.downloaddemo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Created by Will on 2017/3/13.
 */
public class RequestDispatcher extends Thread{
    private ExecutorService mExecutor;
    private BlockingQueue<DownloadTask> mRequestQueue;
    private volatile boolean mQuit = false;

    public RequestDispatcher(ExecutorService executor, BlockingQueue<DownloadTask> requestQueue) {
        mExecutor = executor;
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
                DownloadTask downloadTask = mRequestQueue.take();

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
