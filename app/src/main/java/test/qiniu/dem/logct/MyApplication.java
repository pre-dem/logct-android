package test.qiniu.dem.logct;

import android.app.Application;

import java.io.File;

import qiniu.dem.logct.Config;
import qiniu.dem.logct.LogCT;

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getName();
    private static final String FILE_NAME = "logct_v1";

    @Override
    public void onCreate() {
        super.onCreate();
        initLogCT();
        LogCT.w("MyApplication onCreate", 3);
        LogCT.w("MyApplication onCreate", 3);
        LogCT.w("MyApplication onCreate", 3);
    }

    private void initLogCT() {
        String cachePath = getApplicationContext().getFilesDir().getAbsolutePath();
        String path = getApplicationContext().getExternalFilesDir(null).getAbsolutePath()
                + File.separator + FILE_NAME;
        Config config = new Config.Builder("192.168.31.173:9998", "")
                .setCachePath(cachePath)
                .setPath(path)
                .build();
        LogCT.init(config, getApplicationContext());
        LogCT.setDebug(true);
    }
}
