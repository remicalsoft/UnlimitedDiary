package net.dixq.unlimiteddiary.common;

import android.content.Context;

public class PrefUtils {
    private static final String TAG = "TAG_UNLIMITEDDIARY_PREFERENCE";
    static public void write(Context context, String key, String data) {
        context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .edit()
                .putString(key, data)
                .apply();
    }
    static public String read(Context context, String key){
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getString(key, "");

    }
}
