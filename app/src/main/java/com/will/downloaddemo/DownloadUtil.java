package com.will.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

    public static final String ACTION_PROGRESS = BuildConfig.APPLICATION_ID + "action_progress";
    public static final String ACTION_FINISHED = BuildConfig.APPLICATION_ID + "action_finished";
    public static final String ACTION_PAUSED = BuildConfig.APPLICATION_ID + "action_paused";
    public static final String ACTION_FILE_LENGTH_GET = BuildConfig.APPLICATION_ID + "action_file_length_set";
    public static final String ACTION_FAILED = BuildConfig.APPLICATION_ID + "action_failed";
    public static final String ACTION_NEW_TASK_ADD = BuildConfig.APPLICATION_ID + "action_new_task_add";
    public static final String ACTION_RESUME = BuildConfig.APPLICATION_ID + "action_resume";
    public static final String ACTION_START = BuildConfig.APPLICATION_ID + "action_start";
    public static final String ACTION_REENQUEUE = BuildConfig.APPLICATION_ID + "action_reenqueue";
    public static final String ACTION_CANCELED = BuildConfig.APPLICATION_ID + "action_canceled";

    public static final String EXTRA_TASK_ID = BuildConfig.APPLICATION_ID + "extra_task_id";
    public static final String EXTRA_ERROR_MSG = BuildConfig.APPLICATION_ID + "extra_error_msg";

    final static ExecutorService TASK_EXECUTOR;
    final static Map<String, DownloadRecord> sRecordMap;
    static Semaphore sPermit;
    static int sThreadNum;

    private static DownloadUtil instance;
    private RequestDispatcher mRequestDispatcher;
    private LocalBroadcastManager mBroadcastManager;
    private DataHelper mDataHelper;

    public static final int STATE_INITIAL = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_CANCELED = 4;
    public static final int STATE_FAILED = 5;
    public static final int STATE_WAITING = 6;

    public final static int TIME_OUT = 5000;
    public final static int MAX_CONCURRENT_TASK = 5;
    public final static int DEFAULT_THREAD_NUMBER = 5;

    private boolean initialized = false;

    static {
        TASK_EXECUTOR = Executors.newCachedThreadPool();
        sRecordMap = new LinkedHashMap<>();
        sPermit = new Semaphore(MAX_CONCURRENT_TASK);
        sThreadNum = DEFAULT_THREAD_NUMBER;
    }

    public DownloadUtil init(Context context) {
        if(initialized) return instance;
        mBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
        mDataHelper = new DataHelper(context.getApplicationContext());
        initialized = true;
        return instance;
    }

    public DownloadUtil setMaxConcurrentTask(int number){
        if(initialized) return instance;
        sPermit = new Semaphore(number);
        return instance;
    }

    private DownloadUtil() {
        mRequestDispatcher = new RequestDispatcher();
        mRequestDispatcher.start();
    }

    public static DownloadUtil get() {
        if (instance == null) {
            synchronized (DownloadUtil.class) {
                if (instance == null)
                    instance = new DownloadUtil();
            }
        }
        return instance;
    }

    public void save(){
        mDataHelper.saveAllRecord(sRecordMap.values());
    }

    public String enqueueRequest(DownloadRequest request) {
        mRequestDispatcher.addRequest(request);
        return request.getId();
    }

    public boolean reEnqueue(String taskId) {
        if (sRecordMap.get(taskId) != null) {
            sRecordMap.get(taskId).setDownloadState(STATE_WAITING);
            mRequestDispatcher.enqueueRecord(sRecordMap.get(taskId));
            Intent intent = new Intent(ACTION_REENQUEUE);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            mBroadcastManager.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public boolean pause(String taskId) {
        if (sRecordMap.get(taskId) != null) {
            sRecordMap.get(taskId).setDownloadState(STATE_PAUSED);
            sPermit.release();
            Intent intent = new Intent(ACTION_PAUSED);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            mBroadcastManager.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public boolean cancel(String taskId) {
        if (sRecordMap.get(taskId) != null) {
            sRecordMap.get(taskId).setDownloadState(STATE_CANCELED);
            sPermit.release();
            Intent intent = new Intent(ACTION_CANCELED);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            mBroadcastManager.sendBroadcast(intent);
            return true;
        }
        return false;
    }

    public boolean resume(String taskId) {
        if (sRecordMap.get(taskId) != null) {
            DownloadRecord record = sRecordMap.get(taskId);
            record.setDownloadState(STATE_DOWNLOADING);
            Intent intent = new Intent(ACTION_RESUME);
            intent.putExtra(EXTRA_TASK_ID, taskId);
            mBroadcastManager.sendBroadcast(intent);
            for (int i = 0; i < sThreadNum; i++) {
                TASK_EXECUTOR.execute(record.getSubTaskList().get(i));
            }
            return true;
        }
        return false;
    }

    public void progressUpdated(DownloadRecord record) {
        Intent intent = new Intent(ACTION_PROGRESS);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        mBroadcastManager.sendBroadcast(intent);
    }

    public void taskFinished(DownloadRecord record) {
        sPermit.release();
        record.setDownloadState(STATE_FINISHED);
        Intent intent = new Intent(ACTION_FINISHED);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        mBroadcastManager.sendBroadcast(intent);
    }

    public void fileLengthSet(DownloadRecord record) {
        Intent intent = new Intent(ACTION_FILE_LENGTH_GET);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        mBroadcastManager.sendBroadcast(intent);
    }

    public void downloadFailed(DownloadRecord record, String errorMsg) {
        sPermit.release();
        record.setDownloadState(STATE_FAILED);
        Intent intent = new Intent(ACTION_FAILED);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        intent.putExtra(EXTRA_ERROR_MSG, errorMsg);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void newTaskAdd(DownloadRecord record) {
        Intent intent = new Intent(ACTION_NEW_TASK_ADD);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        mBroadcastManager.sendBroadcast(intent);
    }

    public static DownloadRecord parseRecord(Intent intent) {
        String taskId = intent.getStringExtra(EXTRA_TASK_ID);
        return sRecordMap.get(taskId);
    }

    void start(DownloadRecord record) {
        record.reset();
        record.setDownloadState(STATE_DOWNLOADING);
        new DownloadTask().executeOnExecutor(TASK_EXECUTOR, record);
        Intent intent = new Intent(ACTION_START);
        intent.putExtra(EXTRA_TASK_ID, record.getId());
        mBroadcastManager.sendBroadcast(intent);
    }

    public void registerListener(Context context, final DownloadListener listener) {
        if(listener == null)
            return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEW_TASK_ADD);
        filter.addAction(ACTION_START);
        filter.addAction(ACTION_FILE_LENGTH_GET);
        filter.addAction(ACTION_PROGRESS);
        filter.addAction(ACTION_PAUSED);
        filter.addAction(ACTION_REENQUEUE);
        filter.addAction(ACTION_RESUME);
        filter.addAction(ACTION_FINISHED);
        filter.addAction(ACTION_FAILED);

        LocalBroadcastManager.getInstance(context).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadRecord record = DownloadUtil.parseRecord(intent);
                switch (intent.getAction()) {
                    case ACTION_NEW_TASK_ADD:
                        listener.onNewTaskAdd(record);
                        break;
                    case ACTION_START:
                        listener.onStart(record);
                        break;
                    case ACTION_FILE_LENGTH_GET:
                        listener.onFileLengthGet(record);
                        break;
                    case ACTION_PROGRESS:
                        listener.onProgress(record);
                        break;
                    case ACTION_PAUSED:
                        listener.onPaused(record);
                        break;
                    case ACTION_RESUME:
                        listener.onResume(record);
                        break;
                    case ACTION_REENQUEUE:
                        listener.onReEnqueue(record);
                        break;
                    case ACTION_CANCELED:
                        listener.onCanceled(record);
                        break;
                    case ACTION_FAILED:
                        listener.onFailed(record, intent.getStringExtra(EXTRA_ERROR_MSG));
                        break;
                    case ACTION_FINISHED:
                        listener.onFinish(record);
                        break;
                }
            }
        }, filter);
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
}
