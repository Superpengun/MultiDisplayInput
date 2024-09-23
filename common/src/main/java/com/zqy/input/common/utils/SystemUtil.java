package com.zqy.input.common.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;


/**
 * Created by zhizhou5 on 2017/12/15.
 * 系统工具类 :
 *
 * 1、获取手机制造厂商
   2、获取手机型号
   3、获取手机系统当前使用的语言
   4、获取Android系统版本号
   5、获取手机IMEI串号
   6、获取手机中的语言列表
 */

public class SystemUtil {
    private static final String TAG = SystemUtil.class.getSimpleName();

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppVersionName() {
        try {
            Context context = ContextProvider.Companion.getInstance().getContext();
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Return the application's version name.
     *
     * @param packageName The name of the package.
     * @return the application's version name
     */
    public static String getAppVersionName(final String packageName) {
        if (TextUtils.isEmpty(packageName.trim())) {
            return "";
        }
        try {
            PackageManager pm = ContextProvider.Companion.getInstance().getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    public static String getWeithAndHeight(Context context){
        int width = -1;
        int height = -1;

        try{
            //这种方式在service中无法使用
            DisplayMetrics dm = new DisplayMetrics();
            if(context instanceof Activity) {
                Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
                Class c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                width = dm.widthPixels;              //宽
                height = dm.heightPixels;           //高
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            if(width==-1 || height==-1){
                //在service中也能得到高和宽
                DisplayMetrics dm = new DisplayMetrics();
                WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = mWindowManager.getDefaultDisplay();
                Class c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
                width = dm.widthPixels;              //宽
                height = dm.heightPixels;           //高
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


        return width + "*" + height;
    }

    /**
     * 获取导航栏高度
     *
     * @param context 上下文
     * @return 高度
     */
    public static int getNavigationHeight(Context context) {
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else{
            return 0;
        }
    }


    //NavigationBar状态是否是显示
    public static boolean isNavigationBarShow(Activity mActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(mActivity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }


    /**
     * 获取NavigationBar高度
     *
     */
    public static int getNavigationBarHeight(Context context) {
        return getSizeByReflection(context, "navigation_bar_height");
    }

    public static int getSizeByReflection(Context context, String field) {
        int size = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField(field).get(object).toString());
            size = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

        public static String getDeviceInfo(Context context){
        String language = SystemUtil.getSystemLanguage();
        String systemVer = SystemUtil.getSystemVersion();
        String model = SystemUtil.getSystemModel();
        String brand = SystemUtil.getDeviceBrand();
        String imei = SystemUtil.getIMEI(context);
        String resolution = SystemUtil.getWeithAndHeight(context);

        return "language_" + language + " : systemVer_" + systemVer + " : model_" + model +
                " : brand_" + brand + " : imei_" + imei + ": resolution_" + resolution;
    }

    /**
     * 获取context是否处在栈顶
     * @param context
     * @return
     */
    public static boolean isTopActivity(Context context) {
        boolean isTop = false;
        if (context != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

            String simpleName = context.getClass().getSimpleName();
            if (cn.getClassName().contains(simpleName)) {
                isTop = true;
            }
        }
        return isTop;
    }

    /**
     * 获取当前系统版本
     * @return
     */
    public static int getCurrentOsVer(){
        return Build.VERSION.SDK_INT;
    }

    /**
     * 是否是三星android5.0及以下手机
     * @return
     */
    private static boolean isSamsungBelowAndroid5(){
        int sdk = Build.VERSION.SDK_INT;
        String carrier = Build.MANUFACTURER;
        if (sdk<= Build.VERSION_CODES.LOLLIPOP && carrier!=null && "samsung".equals(carrier.toLowerCase())){
            return true;
        }
        return false;
    }

    /**
     * 是否是小米android4.4.4及以下手机
     * @return
     */
    private static boolean isMUIBelowSDK19(){
        int sdk = Build.VERSION.SDK_INT;
        String carrier = Build.MANUFACTURER;
        if (sdk<= Build.VERSION_CODES.KITKAT && carrier!=null && "xiaomi".equals(carrier.toLowerCase())){
            return true;
        }
        return false;
    }

    /**
     * 忽略沉浸式状态栏的手机厂商和系统版本
     * @return
     */
    public static boolean isDismissVersion(){
        return isSamsungBelowAndroid5() || isMUIBelowSDK19();
    }

    public static String getCurrentProcessName() {
        String name = getCurrentProcessNameByFile();
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        name = getCurrentProcessNameByAms();
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        name = getCurrentProcessNameByReflect();
        return name;
    }
    private static String getCurrentProcessNameByFile() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getCurrentProcessNameByAms() {
        try {
            ActivityManager am = (ActivityManager) ContextProvider.Companion.getInstance().getApplication().getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                return "";
            }
            List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
            if (info == null || info.size() == 0) {
                return "";
            }
            int pid = android.os.Process.myPid();
            for (ActivityManager.RunningAppProcessInfo aInfo : info) {
                if (aInfo.pid == pid) {
                    if (aInfo.processName != null) {
                        return aInfo.processName;
                    }
                }
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    private static String getCurrentProcessNameByReflect() {
        String processName = "";
        try {
            Application app = ContextProvider.Companion.getInstance().getApplication();
            Field loadedApkField = app.getClass().getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(app);

            Field activityThreadField = loadedApk.getClass().getDeclaredField("mActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(loadedApk);

            Method getProcessName = activityThread.getClass().getDeclaredMethod("getProcessName");
            processName = (String) getProcessName.invoke(activityThread);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processName;
    }
    public static boolean isSDCardEnableByEnvironment() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
