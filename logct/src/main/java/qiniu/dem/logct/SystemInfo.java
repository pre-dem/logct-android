package qiniu.dem.logct;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

public class SystemInfo {

    static String packageId;
    static String appName;
    static String appVersion;

     static String osVersion() {
        try {
            String v = android.os.Build.VERSION.RELEASE;
            if (v == null) {
                return "-";
            }
            return strip(v.trim());
        } catch (Throwable t) {
            return "-";
        }
    }

    private static void init(Context context){
         loadPackageData(context);
         appName = getAppName(context);
    }

    private static void loadPackageData(Context context) {
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                packageId = packageInfo.packageName;
                appVersion = packageInfo.versionName;

                int buildNumber = loadBuildNumber(context, packageManager);
                if ((buildNumber != 0) && (buildNumber > packageInfo.versionCode)) {
                    appVersion = "" + buildNumber;
                }
            } catch (Exception e) {
                Log.i("LogCT", "Exception thrown when accessing the package info:" + e.toString());
            }
        }
    }

     static String device() {
        try {
            String model = Build.MODEL.trim();
            String device = deviceName(Build.MANUFACTURER.trim(), model);
            if (TextUtils.isEmpty(device)) {
                device = deviceName(Build.BRAND.trim(), model);
            }
            return strip((device == null ? "-" : device) + "-" + model);
        } catch (Throwable t) {
            return "-";
        }
    }

     static String deviceName(String manufacturer, String model) {
        String str = manufacturer.toLowerCase(Locale.getDefault());
        if ((str.startsWith("unknown")) || (str.startsWith("alps")) ||
                (str.startsWith("android")) || (str.startsWith("sprd")) ||
                (str.startsWith("spreadtrum")) || (str.startsWith("rockchip")) ||
                (str.startsWith("wondermedia")) || (str.startsWith("mtk")) ||
                (str.startsWith("mt65")) || (str.startsWith("nvidia")) ||
                (str.startsWith("brcm")) || (str.startsWith("marvell")) ||
                (model.toLowerCase(Locale.getDefault()).contains(str))) {
            return null;
        }
        return manufacturer;
    }

    public static String strip(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0, length = s.length(); i < length; i++) {
            char c = s.charAt(i);
            if (c > '\u001f' && c < '\u007f') {
                b.append(c);
            }
        }
        return b.toString();
    }

    private static int loadBuildNumber(Context context, PackageManager packageManager) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle metaData = appInfo.metaData;
            if (metaData != null) {
                return metaData.getInt("buildNumber", 0);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("LOGCT", "Exception thrown when accessing the application info:" + e.toString());
//            e.printStackTrace();
        }
        return 0;
    }

    public static String getAppName(Context context) {
        int lableInfo = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.DONUT) {
            lableInfo = context.getApplicationInfo().labelRes;
        }
        return context.getString(lableInfo);
    }
}
