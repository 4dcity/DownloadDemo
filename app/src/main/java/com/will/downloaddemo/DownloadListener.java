package com.will.downloaddemo;

interface DownloadListener{

    void onProgress(DownloadRecord record);

    void onNewTaskAdd(DownloadRecord record);

    void onFailed(DownloadRecord record, String errMsg);

    void onPaused(DownloadRecord record);

    void onStart(DownloadRecord record);

    void onResume(DownloadRecord record);

    void onReEnqueue(DownloadRecord record);

    void onFinish(DownloadRecord record);

    void onFileLengthGet(DownloadRecord record);

    void onCanceled(DownloadRecord record);
}