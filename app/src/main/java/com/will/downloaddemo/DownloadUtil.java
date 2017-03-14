package com.will.downloaddemo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


/**
 * Created by Will on 2017/3/11.
 */

public class DownloadUtil {

    private static final String ACTION_PROGRESS = BuildConfig.APPLICATION_ID + "action_progress";
    private static final String ACTION_FINISHED = BuildConfig.APPLICATION_ID + "action_finished";
    private static final String ACTION_PAUSED = BuildConfig.APPLICATION_ID + "action_paused";
    private static final String ACTION_FILE_LENGTH_SET = BuildConfig.APPLICATION_ID + "action_file_length_set";
    private static final String ACTION_FAILED = BuildConfig.APPLICATION_ID + "action_failed";

    private static final String EXTRA_TASKID = BuildConfig.APPLICATION_ID + "extra_taskid";
    private static final String EXTRA_PROGRESS = BuildConfig.APPLICATION_ID + "extra_progress";
    private static final String EXTRA_ERROR_MSG = BuildConfig.APPLICATION_ID + "extra_error_msg";

    private static DownloadUtil instance;

    final static ExecutorService TASK_EXECUTOR;
    static Map<String, DownloadRecord> sRecordMap;
    static Semaphore sPermit;
    private Context mAppContext;
    private RequestDispatcher mRequestDispatcher;
    private LocalBroadcastManager broadcastManager;

    public static final int MSG_FINISHED = 1;
    public static final int MSG_PROGRESS = 2;
    public static final int MSG_CANCELED = 3;
    public static final int MSG_PAUSED = 4;
    public static final int MSG_FAILED = 5;

    public static final int STATE_INITIAL = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_CANCELED = 4;
    public static final int STATE_FAILED = 5;

    public final static int TIME_OUT = 5000;
    public final static int THREAD_NUM = 5;

    static{
        TASK_EXECUTOR = Executors.newCachedThreadPool();
        sRecordMap = new LinkedHashMap<>();
        sPermit = new Semaphore(3);
    }

    public void init(Context context){
        mAppContext = context.getApplicationContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    private DownloadUtil() {
        mRequestDispatcher = new RequestDispatcher();
        mRequestDispatcher.start();
    }

    public static DownloadUtil get(){
        if (instance == null){
            synchronized(DownloadUtil.class){
                if (instance == null)
                    instance = new DownloadUtil();
            }
        }
        return instance;
    }

    public Context getContext(){
        return mAppContext;
    }

    public String enqueueRequest(DownloadRequest request){
        mRequestDispatcher.addRequest(request);
        return request.getId();
    }

    void enqueueRecord(DownloadRecord record){
        mRequestDispatcher.enqueueRecord(record);
    }

    public void pause(String taskId){
        sRecordMap.get(taskId).setDownloadState(STATE_PAUSED);
        sPermit.release();
    }

    /**
     * 回复下载后要重新排队
     * @param taskId
     */
    public void resume(String taskId){
        sRecordMap.get(taskId).setDownloadState(STATE_DOWNLOADING);
        enqueueRecord(sRecordMap.get(taskId));
    }

    public int getTaskState(String taskId){
        return sRecordMap.get(taskId).getDownloadState();
    }


    public static String getMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    public void progressUpdated(DownloadRecord record){
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(EXTRA_TASKID, record.getId());
        intent.putExtra(EXTRA_PROGRESS, record.getProgress());
        broadcastManager.sendBroadcast(intent);
    }

    public void taskFinished(DownloadRecord record) {
        sRecordMap.remove(record.getId());
        sPermit.release();
        Intent intent = new Intent(ACTION_FINISHED);
        intent.putExtra(EXTRA_TASKID, record.getId());
        broadcastManager.sendBroadcast(intent);
    }

    public void fileLengthSet(DownloadRecord record) {
        Intent intent = new Intent(ACTION_FILE_LENGTH_SET);
        intent.putExtra(EXTRA_TASKID, record.getId());
        broadcastManager.sendBroadcast(intent);
    }

    public void downloadFailed(DownloadRecord record, String errorMsg) {
        sPermit.release();
        Intent intent = new Intent(ACTION_FAILED);
        intent.putExtra(EXTRA_TASKID, record.getId());
        intent.putExtra(EXTRA_ERROR_MSG, errorMsg);
        broadcastManager.sendBroadcast(intent);
    }

}
