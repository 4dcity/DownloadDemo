package com.will.downloaddemo;

import android.os.Environment;

/**
 * Created by Will on 2017/3/13.
 */

public class DownloadRequest {

    private String downloadUrl;
    private String saveDir;
    private String saveName;

    private DownloadRequest(Builder builder) {
        downloadUrl = builder.downloadUrl;
        saveDir = builder.saveDir;
        saveName = builder.saveName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getSaveName() {
        return saveName;
    }

    public static final class Builder {
        private String downloadUrl;
        private String saveDir = Environment.getExternalStorageDirectory().toString();
        private String saveName = "/" + System.currentTimeMillis();

        private Builder() {
        }

        public Builder downloadUrl(String val) {
            downloadUrl = val;
            return this;
        }

        public Builder saveDir(String val) {
            saveDir = val;
            return this;
        }

        public Builder saveName(String val) {
            saveName = val;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(this);
        }
    }
}
