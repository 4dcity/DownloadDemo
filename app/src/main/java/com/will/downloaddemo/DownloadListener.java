package com.will.downloaddemo;

import java.net.HttpURLConnection;

interface DownloadListener{

    void onProgress(int progress);

    void onSuccess();

    void onChildComplete(long endLocation);

    void onPause(int currentLocation);

    void onPreDownload(HttpURLConnection conn);

    void onFail();

    void onComplete();

    void onChildResume(Long r);

    void onResume(int currentLocation);

    void onStart();

    void onCancel();
}