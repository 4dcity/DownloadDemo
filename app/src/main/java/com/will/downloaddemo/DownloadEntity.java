package com.will.downloaddemo;

import android.content.Context;

import java.io.File;

public class DownloadEntity {
    //文件总长度
    public long fileSize;
    //下载链接
    public String downloadUrl;
    //线程Id
    public int threadId;
    //起始下载位置
    public long startLocation;
    //结束下载的文章
    public long endLocation;
    //下载文件
    public File tempFile;

    public Context context;

    public DownloadEntity(Context context, long fileSize, String downloadUrl, File file, int threadId, long startLocation, long endLocation) {
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.tempFile = file;
        this.threadId = threadId;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.context = context;
    }
}