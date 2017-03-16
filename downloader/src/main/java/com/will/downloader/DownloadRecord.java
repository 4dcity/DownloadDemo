package com.will.downloader;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2017/3/13.
 */

public class DownloadRecord implements Comparable<DownloadRecord>{
    @Expose private final DownloadRequest request;
    @Expose private volatile int downloadState;
    @Expose private int currentLength;
    @Expose private int fileLength;
    @Expose private int completedSubTask;
    @Expose private List<SubTask> subTaskList;
    @Expose private long createTime;

    DownloadRecord(DownloadRequest request) {
        this.request = request;
        subTaskList = new ArrayList<>();
        downloadState = DownloadUtil.STATE_INITIAL;
        createTime = System.currentTimeMillis();
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public int getFileLength() {
        return fileLength;
    }

    void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public String getDownloadUrl() {
        return request.getDownloadUrl();
    }

    public String getDownloadDir() {
        return request.getDownloadDir();
    }

    public String getDownloadName() {
        return request.getDownloadName();
    }

    public List<SubTask> getSubTaskList() {
        return subTaskList;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public long getCreateTime() {
        return createTime;
    }

    void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    synchronized boolean completeSubTask(){
        completedSubTask++;
        if(completedSubTask == subTaskList.size()){
            return true;
        }
        return false;
    }

    synchronized void increaseLength(int length) {
        currentLength+=length;
    }

    public String getFilePath() {
        return getDownloadDir() + "/" + getDownloadName();
    }

    public String getId() {
        return request.getId();
    }

    public int getProgress(){
        return Math.round(getCurrentLength() / (getFileLength() * 1.0f) * 100);
    }

    void reset(){
        currentLength = 0;
        fileLength = 0;
        completedSubTask = 0;
        subTaskList.clear();
    }

    void linkSubTask(){
        for (SubTask subTask : subTaskList) {
            subTask.setRecord(this);
        }
    }

    @Override
    public int compareTo(@NonNull DownloadRecord o) {
        if(createTime < o.getCreateTime()) return -1;
        if(createTime > o.getCreateTime()) return 1;
        return 0;
    }
}
