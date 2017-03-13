package com.will.downloaddemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.textView)
    TextView txtProgress;
    @BindView(R.id.button)
    Button btnStart;
    @BindView(R.id.button2)
    Button btnPause;
    @BindView(R.id.button3)
    Button btnCancel;

    String mDownloadUrl;
    String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressBar.setMax(100);
        mDownloadUrl = "http://ftp-apk.pconline.com.cn/4da968ab4fd592239194501261cce88a/pub/download/201010/com.sdu.didi.psnger-v4.4.4_55032.apk";
        mFilePath = Environment.getExternalStorageDirectory() + "/didi.apk";
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                new DownloadTask(mDownloadUrl, mFilePath, new DownloadCallback(){
                    @Override
                    public void onProgress(int progress) {
                        progressBar.setProgress(progress);
                        txtProgress.setText(progress + "%");
                    }

                    @Override
                    public void onSuccess() {
                        txtProgress.setText("完成");
                    }
                }).start();
                break;
            case R.id.button2:
                break;
            case R.id.button3:
                break;
        }
    }

}
