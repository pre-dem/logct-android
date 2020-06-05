package test.qiniu.dem.logct;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import qiniu.dem.logct.DeviceInfo;
import qiniu.dem.logct.LogCT;
import qiniu.dem.logct.UploadHandler;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    private TextView mTvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        Button button = (Button) findViewById(R.id.write_btn);
        Button batchBtn = (Button) findViewById(R.id.write_batch_btn);
        Button logFileBtn = (Button) findViewById(R.id.show_log_file_btn);
        mTvInfo = (TextView) findViewById(R.id.info);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogCT.w("LogCT GOGOGO", 2);
            }
        });
        batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCTTest();
            }
        });

        logFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCTFilesInfo();
            }
        });
        findViewById(R.id.send_btn_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCTSend();
            }
        });
    }

    private void logCTTest() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    for (int i = 0; i < 9; i++) {
                        Log.d(TAG, "times : " + i);
                        LogCT.w(String.valueOf(i), 1);
                        Thread.sleep(5);
                    }
                    Log.d(TAG, "write log end");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void logCTFilesInfo() {
        Map<String, Long> map = LogCT.getAllFilesInfo();
        if (map != null) {
            StringBuilder info = new StringBuilder();
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                info.append("文件日期：").append(entry.getKey()).append("  文件大小（bytes）：").append(
                        entry.getValue()).append("\n");
            }
            mTvInfo.setText(info.toString());
        }
    }

    private void logCTSend() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String date = dateFormat.format(new Date(System.currentTimeMillis()));
        LogCT.setDeviceInfo(new DeviceInfo("testUser", "testDevice"));
        LogCT.upload(date, new UploadHandler() {
            @Override
            public void complete(int code, String data) {
                final String resultData = data != null ? new String(data) : "";
                Log.d(TAG, "日志上传结果, http状态码: " + code + ", 详细: " + resultData);
            }
        });
    }
}
