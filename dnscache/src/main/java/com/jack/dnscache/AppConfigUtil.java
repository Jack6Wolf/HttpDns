package com.jack.dnscache;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.io.File;

public class AppConfigUtil {

    private static Context mContext;

    public static void init(Context ctx) {
        mContext = ctx;
    }

    public static Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    /**
     * 获取缓存文件夹
     *
     * @return
     */
    public static File getExternalCacheDir() {
        return mContext.getExternalCacheDir();
    }

    /**
     * 返回当前程序版本名
     *
     * @return
     */
    public static String getVersionName() {
        String versionName = "";
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取设备id
     *
     * @return
     */
    public static String getDeviceId() {
        String deviceId = Build.SERIAL;
        return deviceId;
    }

    /**
     * 获取当前的应用key
     *
     * @return
     */
    public static String getAppKey() {
        try {
            ApplicationInfo appInfo = mContext.getPackageManager()
                    .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (null != metaData) {
                String appKey = metaData.getString("DNSCACHE_APP_KEY");
                return appKey;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
