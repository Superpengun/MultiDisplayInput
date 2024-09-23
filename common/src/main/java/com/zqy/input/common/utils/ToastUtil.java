package com.zqy.input.common.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.zqy.common.R;


/**
 * @author liufenghui
 */
public class ToastUtil {

    private static long lastClickTime;
    private static Toast newestToast;

    // 防止连续点击按钮
    private synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime > 0 && time - lastClickTime < 2000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void centerShowToast(Context context, String message) {
        if (context == null) {
            return;
        }
        if (!isFastClick()) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                TextView textview = new TextView(context);
                textview.setText(message);
                textview.setTextSize(14);
                textview.setGravity(Gravity.CENTER);
                textview.setTextColor(Color.WHITE);
                textview.setPadding(35,15,35,15);
                textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
                toast.setView(textview);
                toast.show();
            } catch (Exception | OutOfMemoryError e) {
                Log.e("show toast failed", e.toString());
            }
        }
    }

    public static void centerShowToast(Context context, int messageRes) {
        if (context == null) {
            return;
        }
        try {
            String message = context.getString(messageRes);
            centerShowToast(context, message);
        } catch (Exception e){
            Log.e("show toast failed", e.toString());
        }
    }

    public static void bottomShowToast(Context context, String message) {
        if (context == null) {
            return;
        }
        if (!isFastClick()) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                TextView textview = new TextView(context);
                textview.setText(message);
                textview.setTextSize(14);
                textview.setGravity(Gravity.CENTER);
                textview.setTextColor(Color.WHITE);
                textview.setPadding(35,15,35,15);
                textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
                toast.setView(textview);
                toast.show();
            } catch (Exception | OutOfMemoryError e) {
                Log.e("show toast failed", e.toString());
            }
        }
    }

    public static void bottomShowToast(Context context, int messageRes) {
        if (context == null) {
            return;
        }
        try {
            String message = context.getString(messageRes);
            bottomShowToast(context, message);
        } catch (Exception e){
            Log.e("show toast failed", e.toString());
        }
    }

    public static void centerShowToastDurationLong(Context context, String message) {
        if (context == null) {
            return;
        }
        if (!isFastClick()) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                TextView textview = new TextView(context);
                textview.setText(message);
                textview.setTextSize(14);
                textview.setGravity(Gravity.CENTER);
                textview.setTextColor(Color.WHITE);
                textview.setPadding(35,15,35,15);
                textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
                toast.setView(textview);
                toast.show();
            } catch (Exception | OutOfMemoryError e) {
                Log.e("show toast failed", e.toString());
            }
        }
    }

    public static void bottomShowToastDurationLong(Context context, String message) {
        if (context == null) {
            return;
        }
        if (!isFastClick()) {
            try {
                Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                TextView textview = new TextView(context);
                textview.setText(message);
                textview.setTextSize(14);
                textview.setGravity(Gravity.CENTER);
                textview.setTextColor(Color.WHITE);
                textview.setPadding(35,15,35,15);
                textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
                toast.setView(textview);
                toast.show();
            } catch (Exception | OutOfMemoryError e) {
                Log.e("show toast failed", e.toString());
            }
        }
    }

    /**
     * 覆盖显示，只显示最后一条最新消息
     * @param context
     * @param msg
     */
    public static void centerShowNewestToast(Context context, int msg) {
        if (context == null) {
            return;
        }
        try {
            String message = context.getString(msg);
            centerShowNewestToast(context, message);
        } catch (Exception e){
            Log.e("show toast failed", e.toString());
        }
    }

    /**
     * 覆盖显示，只显示最后一条最新消息
     * @param context
     * @param message
     */
    public static void centerShowNewestToast(Context context, String message) {
        if (context == null) {
            return;
        }

        try {
            if (newestToast == null) {
                newestToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
                newestToast.setGravity(Gravity.CENTER, 0, 0);
            }
            TextView textview = new TextView(context.getApplicationContext());
            textview.setText(message);
            textview.setTextSize(14);
            textview.setGravity(Gravity.CENTER);
            textview.setTextColor(Color.WHITE);
            textview.setPadding(35,15,35,15);
            textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
            newestToast.setView(textview);
            newestToast.show();
        } catch (Exception | OutOfMemoryError e) {
            Log.e("show toast failed", e.toString());
        }
    }

    /**
     * 覆盖显示，只显示最后一条最新消息
     * @param context
     * @param message
     */
    public static void centerShowNewestToast(Context context, SpannableStringBuilder message) {
        if (context == null) {
            return;
        }

        try {
            if (newestToast == null) {
                newestToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
                newestToast.setGravity(Gravity.CENTER, 0, 0);
            }
            TextView textview = new TextView(context.getApplicationContext());
            textview.setText(message);
            textview.setTextSize(14);
            textview.setGravity(Gravity.CENTER);
            textview.setTextColor(Color.WHITE);
            textview.setPadding(35,15,35,15);
            textview.setBackgroundResource(R.drawable.toast_bg_dis_gray);
            newestToast.setView(textview);
            newestToast.show();
        } catch (Exception | OutOfMemoryError e) {
            Log.e("show toast failed", e.toString());
        }
    }

    /**
     * 在子线程中显示toast
     * @param context
     * @param messageRes
     */
    public static void centerShowToastInChildThread(final Context context, int messageRes) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            centerShowToast(context,messageRes);
        }else {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Looper.prepare();
                    centerShowToast(context,messageRes);
                    Looper.loop();
                }
            }.start();
        }
    }

    /**
     * 在子线程中显示toast
     * @param context
     * @param message
     */
    public static void centerShowToastInChildThread(final Context context, String message) {
        if(Looper.myLooper() == Looper.getMainLooper()){
            centerShowToast(context,message);
        }else {
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    Looper.prepare();
                    centerShowToast(context,message);
                    Looper.loop();
                }
            }.start();
        }
    }

}
