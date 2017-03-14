package com.will.downloaddemo;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.will.downloaddemo.DownloadUtil.STATE_DOWNLOADING;
import static com.will.downloaddemo.DownloadUtil.STATE_PAUSED;

/**
 * Created by Will on 2017/3/14.
 */

public class TaskListAdapter extends ArrayAdapter<DownloadRecord> {

    private LayoutInflater mLayoutInflater;

    public TaskListAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        init(context);
    }

    public TaskListAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId) {
        super(context, resource, textViewResourceId);
        init(context);
    }

    private void init(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        final DownloadRecord record = getItem(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_download_task, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
            record.setListener(new DownloadCallback(){
                @Override
                public void onProgress(int progress) {
                    viewHolder.progressBar.setProgress(progress);
                }

                @Override
                public void onSuccess() {
                    viewHolder.tvProgress.setText("完成");
                }
            });
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (record.getDownloadState()){
            case STATE_DOWNLOADING:
                viewHolder.btnState.setText("暂停");
                break;
            case STATE_PAUSED:
                viewHolder.btnState.setText("继续");
        }

        viewHolder.btnState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (record.getDownloadState()){
                    case STATE_DOWNLOADING:
                        record.pauseDownload();
                        break;
                    case STATE_PAUSED:
                        record.resumeDownload();
                        break;
                }
            }
        });
        viewHolder.tvTaskName.setText(record.getSaveName());

        return convertView;
    }


    static class ViewHolder {
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.tvProgress)
        TextView tvProgress;
        @BindView(R.id.btnState)
        Button btnState;
        @BindView(R.id.tvTaskName)
        TextView tvTaskName;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
