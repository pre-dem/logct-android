/*
 * Copyright (c) 2018-present, 美团点评
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package test.qiniu.dem.logct;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText mEditIp;
    private RealSendLogRunnable mSendLogRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mSendLogRunnable = new RealSendLogRunnable();
    }

    private void initView() {
        Button button = (Button) findViewById(R.id.write_btn);
        Button batchBtn = (Button) findViewById(R.id.write_batch_btn);
        Button sendBtn = (Button) findViewById(R.id.send_btn);
        Button logFileBtn = (Button) findViewById(R.id.show_log_file_btn);
        mTvInfo = (TextView) findViewById(R.id.info);
        mEditIp = (EditText) findViewById(R.id.send_ip);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogCT.w("啊哈哈哈哈66666", 2);
            }
        });
        batchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCTTest();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logCTSend();
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
                logCTSendByDefault();
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

    private void logCTSend() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        String d = dataFormat.format(new Date(System.currentTimeMillis()));
        LogCT.upload(d, new UploadHandler() {
            @Override
            public void complete(int code, String data) {
                System.out.println("code" + code);
            }
        });
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

    private void logCTSendByDefault() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String date = dataFormat.format(new Date(System.currentTimeMillis()));
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
