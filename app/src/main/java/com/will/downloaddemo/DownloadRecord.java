package com.will.downloaddemo;

import java.util.ArrayList;
import java.util.List;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.STATE_PAUSED;
import static com.will.downloaddemo.DownloadUtil.THREAD_NUM;
import static com.will.downloaddemo.DownloadUtil.sPermit;

/**
 * Created by Will on 2017/3/13.
 */

public class DownloadRecord {
    private final DownloadRequest request;
    private int currentLength;
    private int fileLength;
    private int completedSubTask;
    private volatile int downloadState;
    private List<SubTask> subTaskList;

    public DownloadRecord(DownloadRequest request) {
        this.request = request;
        subTaskList = new ArrayList<>();
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

    public String getDownloadUrl() {
        return request.getDownloadUrl();
    }

    public String getSaveDir() {
        return request.getSaveDir();
    }

    public String getSaveName() {
        return request.getSaveName();
    }

    public int getCompletedSubTask() {
        return completedSubTask;
    }

    public void setCompletedSubTask(int completedSubTask) {
        this.completedSubTask = completedSubTask;
    }

    public List<SubTask> getSubTaskList() {
        return subTaskList;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
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

    public boolean isPaused() {
        return downloadState == STATE_PAUSED;
    }

    public String getFilePath() {
        return getSaveDir() + "/" + getSaveName();
    }

    public String getId() {
        return request.getId();
    }

    public int getProgress(){
        return Math.round(getCurrentLength() / (getFileLength() * 1.0f) * 100);
    }
}
