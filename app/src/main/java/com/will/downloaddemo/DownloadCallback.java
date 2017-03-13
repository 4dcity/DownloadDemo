package com.will.downloaddemo;

import java.net.HttpURLConnection;

/**
 * Created by Will on 2017/3/12.
 */

public class DownloadCallback implements DownloadListener {
    @Override
    public void onProgress(int progress) {

    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onChildComplete(long endLocation) {

    }

    @Override
    public void onPause(int currentLocation) {

    }

    @Override
    public void onPreDownload(HttpURLConnection conn) {

    }

    @Override
    public void onFail() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onChildResume(Long r) {

    }

    @Override
    public void onResume(int currentLocation) {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCancel() {

    }
}
