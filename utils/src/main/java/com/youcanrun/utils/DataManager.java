package com.youcanrun.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {
private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void saveData(String key_one, String key_two, float data){
        SharedPreferences highScores_Manager = context.getSharedPreferences(key_one, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = highScores_Manager.edit();
        editor.putFloat(key_two, data);
        editor.apply();
    }

    public float loadData(String key_one, String key_two){
        SharedPreferences highScores_Manager = context.getSharedPreferences(key_one, Context.MODE_PRIVATE);
        return highScores_Manager.getFloat(key_two,0f);
    }
}
