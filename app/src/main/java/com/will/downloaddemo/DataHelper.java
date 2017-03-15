package com.will.downloaddemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collection;

/**
 * Created by Will on 2017/3/15.
 */

class DataHelper {
    private SharedPreferences sp;
    private Gson gson;

    DataHelper(Context context) {
        sp = context.getSharedPreferences("download_task", Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
    }

    void saveAllRecord(Collection<DownloadRecord> records){
        for (DownloadRecord record : records) {
            String json = gson.toJson(record);
            sp.edit().putString(record.getId(), json).apply();
        }
    }

}
