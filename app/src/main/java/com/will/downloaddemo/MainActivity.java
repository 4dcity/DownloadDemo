package com.will.downloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.will.downloaddemo.DownloadUtil.ACTION_FAILED;
import static com.will.downloaddemo.DownloadUtil.ACTION_FILE_LENGTH_SET;
import static com.will.downloaddemo.DownloadUtil.ACTION_FINISHED;
import static com.will.downloaddemo.DownloadUtil.ACTION_NEW_TASK_ADD;
import static com.will.downloaddemo.DownloadUtil.ACTION_PAUSED;
import static com.will.downloaddemo.DownloadUtil.ACTION_PROGRESS;
import static com.will.downloaddemo.DownloadUtil.ACTION_RESUME;
import static com.will.downloaddemo.DownloadUtil.ACTION_START;
import static com.will.downloaddemo.DownloadUtil.EXTRA_ERROR_MSG;
import static com.will.downloaddemo.DownloadUtil.sRecordMap;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edtUrl)
    EditText edtUrl;
    @BindView(R.id.btnAdd)
    Button btnAdd;
    @BindView(R.id.rvTasks)
    RecyclerView rvTasks;

    String mDownloadUrl = "http://ftp-apk.pconline.com.cn/4da968ab4fd592239194501261cce88a/pub/download/201010/com.sdu.didi.psnger-v4.4.4_55032.apk";
    TaskListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        DownloadUtil.get().init(this);
        edtUrl.setText(mDownloadUrl);
        adapter = new TaskListAdapter(this);
        adapter.setData(sRecordMap.values());
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PROGRESS);
        filter.addAction(ACTION_FINISHED);
        filter.addAction(ACTION_PAUSED);
        filter.addAction(ACTION_FILE_LENGTH_SET);
        filter.addAction(ACTION_FAILED);
        filter.addAction(ACTION_NEW_TASK_ADD);
        filter.addAction(ACTION_START);
        filter.addAction(ACTION_RESUME);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DownloadRecord record = DownloadUtil.parseRecord(intent);
                switch (intent.getAction()){
                    case ACTION_PROGRESS:
                        int index = findRecordIndex(record);
                        adapter.notifyItemChanged(index, "payload");
                        break;
                    case ACTION_NEW_TASK_ADD:
                        adapter.setData(sRecordMap.values());
                        adapter.notifyDataSetChanged();
                        break;
                    case ACTION_FAILED:
                        Toast.makeText(MainActivity.this,
                                intent.getStringExtra(EXTRA_ERROR_MSG),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        }, filter);
    }

    private int findRecordIndex(DownloadRecord record) {
        for(int i = 0; i<adapter.getItemCount(); i++){
            if(record == adapter.getItem(i))
                return i;
        }
        return -1;
    }

    @OnClick(R.id.btnAdd)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAdd:
                if (!TextUtils.isEmpty(edtUrl.getText().toString())) {
                    DownloadRequest request = DownloadRequest.newBuilder()
                            .downloadUrl(edtUrl.getText().toString())
                            .saveName(System.currentTimeMillis() / 1000 + ".apk")
                            .build();
                    DownloadUtil.get().enqueueRequest(request);
                }
                break;
        }
    }


//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.button:
////                DownloadRequest request = DownloadRequest.newBuilder()
////                        .downloadUrl(mDownloadUrl)
////                        .saveName("didi.apk")
////                        .downloadListener(new DownloadCallback(){
////                            @Override
////                            public void onProgress(int progress) {
////                                progressBar.setProgress(progress);
////                                txtProgress.setText(progress + "%");
////                            }
////
////                            @Override
////                            public void onSuccess() {
////                                txtProgress.setText("完成");
////                            }
////                        })
////                        .build();
////
////                handle = DownloadUtil.get().reEnqueue(request);
//                break;
////            case R.id.button2:
////                if(DownloadUtil.get().getTaskState(handle) == STATE_DOWNLOADING){
////                    DownloadUtil.get().pause(handle);
////                }else {
////                    DownloadUtil.get().resume(handle);
////                }
////                break;
////            case R.id.button3:
////                break;
//        }
//    }

}
