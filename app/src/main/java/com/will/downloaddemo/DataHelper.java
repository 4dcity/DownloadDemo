package com.will.downloaddemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
            saveRecord(record);
        }
    }

    void saveRecord(DownloadRecord record){
        String json = gson.toJson(record);
        sp.edit().putString(record.getId(), json).apply();
    }

    List<DownloadRecord> loadAllRecords(){
        Map<String, String> map = (Map<String, String>) sp.getAll();
        List<DownloadRecord> list = new ArrayList<>();
        for (String json: map.values()) {
            DownloadRecord record = gson.fromJson(json, DownloadRecord.class);
            record.linkSubTask();
            list.add(record);
        }
        return list;
    }

}
