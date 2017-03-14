package com.will.downloaddemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.edtUrl)
    EditText edtUrl;
    @BindView(R.id.btnAdd)
    Button btnAdd;
    @BindView(R.id.lvTasks)
    ListView lvTasks;

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
        edtUrl.setText(mDownloadUrl);
        adapter = new TaskListAdapter(this, 0);
        lvTasks.setAdapter(adapter);
    }

    @OnClick(R.id.btnAdd)
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAdd:
                if(!TextUtils.isEmpty(edtUrl.getText().toString())) {
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
////                handle = DownloadUtil.get().enqueueRecord(request);
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
