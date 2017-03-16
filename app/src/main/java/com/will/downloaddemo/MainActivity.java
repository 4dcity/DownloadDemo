package com.will.downloaddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.will.downloaddemo.DownloadUtil.sRecordMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DownloadActivity";
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
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        DownloadUtil.get().destroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    private void init() {
        DownloadUtil.get().init(this);
        edtUrl.setText(mDownloadUrl);
        adapter = new TaskListAdapter(this);
        adapter.setData(sRecordMap.values());
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);
        DownloadUtil.get().registerListener(this, new DownloadCallback() {
            @Override
            public void onProgress(DownloadRecord record) {
                int index = findRecordIndex(record);
                adapter.notifyItemChanged(index, "payload");
            }

            @Override
            public void onNewTaskAdd(DownloadRecord record) {
                adapter.setData(sRecordMap.values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(DownloadRecord record, String errMsg) {
                Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStart(DownloadRecord record) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish(DownloadRecord record) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPaused(DownloadRecord record) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onReEnqueue(DownloadRecord record) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onResume(DownloadRecord record) {
                adapter.notifyDataSetChanged();
            }
        });
    }

//    private void registerReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_PROGRESS);
//        filter.addAction(ACTION_FINISHED);
//        filter.addAction(ACTION_PAUSED);
//        filter.addAction(ACTION_FILE_LENGTH_GET);
//        filter.addAction(ACTION_FAILED);
//        filter.addAction(ACTION_NEW_TASK_ADD);
//        filter.addAction(ACTION_START);
//        filter.addAction(ACTION_RESUME);
//        filter.addAction(ACTION_REENQUEUE);
//
//        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                DownloadRecord record = DownloadUtil.parseRecord(intent);
//                switch (intent.getAction()){
//                    case ACTION_PROGRESS:
//                        int index = findRecordIndex(record);
//                        adapter.notifyItemChanged(index, "payload");
//                        break;
//                    case ACTION_NEW_TASK_ADD:
//                        adapter.setData(sRecordMap.values());
//                        adapter.notifyDataSetChanged();
//                        break;
//                    case ACTION_FAILED:
//                        Toast.makeText(MainActivity.this,
//                                intent.getStringExtra(EXTRA_ERROR_MSG),
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    default:
//                        adapter.notifyDataSetChanged();
//                        break;
//                }
//            }
//        }, filter);
//    }

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
                            .downloadName(System.currentTimeMillis() / 1000 + ".apk")
                            .build();
                    DownloadUtil.get().enqueue(request);
                }
                break;
        }
    }

}
