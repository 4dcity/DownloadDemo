package com.will.downloader;

import android.os.Environment;

import com.google.gson.annotations.Expose;

/**
 * Created by Will on 2017/3/13.
 */

public class DownloadRequest{

    @Expose private final String downloadUrl;
    @Expose private final String downloadDir;
    @Expose private final String downloadName;

    private DownloadRequest(Builder builder) {
        downloadUrl = builder.downloadUrl;
        downloadDir = builder.downloadDir;
        downloadName = builder.downloadName;
    }

    public String getFilePath() {
        return getDownloadDir() + "/" + getDownloadName();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getDownloadName() {
        return downloadName;
    }

    public String getId(){
        return DownloadUtil.getMD5(downloadUrl+ downloadDir + downloadName);
    }

    public static final class Builder {
        private String downloadUrl;
        private String downloadDir = Environment.getExternalStorageDirectory().toString();
        private String downloadName = "/" + System.currentTimeMillis();
        private DownloadListener listener;

        private Builder() {
        }

        public Builder downloadUrl(String val) {
            downloadUrl = val;
            return this;
        }

        public Builder downloadDir(String val) {
            downloadDir = val;
            return this;
        }

        public Builder downloadName(String val) {
            downloadName = val;
            return this;
        }

        public Builder downloadListener(DownloadListener val){
            listener = val;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(this);
        }
    }
}
