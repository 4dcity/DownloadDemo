package com.will.downloaddemo;

import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.STATE_PAUSED;
import static com.will.downloaddemo.DownloadUtil.TASK_EXECUTOR;
import static com.will.downloaddemo.DownloadUtil.THREAD_NUM;

/**
 * Created by Will on 2017/3/13.
 */

public class DownloadRequest {

    private String downloadUrl;
    private String saveDir;
    private String saveName;

    private int currentLength;
    private int fileLength;
    private int completedSubTask;
    private volatile int downloadState;
    private DownloadListener listener;

    private List<DownloadTask.SubTask> subTaskList;
    private DownloadTask downloadTask;

    private DownloadRequest(Builder builder) {
        downloadUrl = builder.downloadUrl;
        saveDir = builder.saveDir;
        saveName = builder.saveName;
        listener = builder.listener;
        subTaskList = new ArrayList<>();
    }

    public void setDownloadTask(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(int currentLength) {
        this.currentLength = currentLength;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }


    public List<DownloadTask.SubTask> getSubTaskList() {
        return subTaskList;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public DownloadListener getListener() {
        return listener;
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public boolean completeSubTask(){
        completedSubTask++;
        if(completedSubTask == THREAD_NUM){
            return true;
        }
        return false;
    }

    public synchronized void increaseLength(int length) {
        currentLength+=length;
    }

    public void pauseDownload(){
        downloadState = STATE_PAUSED;
    }

    public boolean isPaused() {
        return downloadState == STATE_PAUSED;
    }

    public String getFilePath() {
        return getSaveDir() + "/" + getSaveName();
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

    public void resumeDownload() {
        setDownloadState(STATE_DOWNLOADING);
        for (int i = 0; i < THREAD_NUM; i++) {
            DownloadTask.SubTask subTask = subTaskList.get(i);
            TASK_EXECUTOR.execute(subTask);
        }
    }

    public static final class Builder {
        private String downloadUrl;
        private String saveDir = Environment.getExternalStorageDirectory().toString();
        private String saveName = "/" + System.currentTimeMillis();
        private DownloadListener listener;

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

        public Builder downloadListener(DownloadListener val){
            listener = val;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(this);
        }
    }
}
