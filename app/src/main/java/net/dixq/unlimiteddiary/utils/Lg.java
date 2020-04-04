package net.dixq.unlimiteddiary.utils;

import android.annotation.SuppressLint;
import android.util.Log;

public class Lg {
    private final static String TAG = "UnlimitedDiary";

    public static void e(String msg){
        Log.e(TAG, getMsg(msg));
    }

    public static void w(String msg){
        Log.w(TAG, getMsg(msg));
    }

    public static void i(String msg){
        Log.i(TAG, getMsg(msg));
    }

    public static void d(String msg){
        Log.d(TAG, getMsg(msg));
    }

    @SuppressLint("DefaultLocale")
    private static String getMsg(String msg){
        String className = Thread.currentThread().getStackTrace()[4].getClassName();
        return String.format("%s | %s#%s(%d)",
                msg,
                className.substring(className.lastIndexOf('.')+1),
                Thread.currentThread().getStackTrace()[4].getMethodName(),
                Thread.currentThread().getStackTrace()[4].getLineNumber() );
    }
}
