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

package qiniu.dem.logct;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCtTest {
    private static final String TAG = LogCtTest.class.getName();
    private static final String FILE_NAME = "logct" + "_vtest";

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private CountDownLatch mLatch;

    @Before
    public void setUp() throws Exception {
        mLatch = new CountDownLatch(1);
    }

    @Test
    public void test001Init() {
        Context applicationContext = InstrumentationRegistry.getTargetContext();
        String cachePath = applicationContext.getFilesDir().getAbsolutePath();
        String path = applicationContext.getExternalFilesDir(null).getAbsolutePath()
                + File.separator + FILE_NAME;
        Config config = new Config.Builder("http://bhk5aaghth5n.predem.qiniuapi.com", "BawEHOtHwsvrgiAqbGFujmLc")
                .setCachePath(cachePath)
                .setPath(path)
                .build();
        LogCT.init(config);
        LogCT.setDebug(true);
    }

    @Test
    public void test002Write() throws InterruptedException {
        LogCT.w("junit test write function", 1);
        assertWriteLog();
    }

    @Test
    public void test003Flush() {
        LogCT.flush();
    }

    @Test
    public void test004Upload() {
        LogCT.upload(getTodayDate(), new UploadHandler() {
            @Override
            public void complete(int code, String data) {
                System.out.println("today " + code);
            }
        });
    }

    @Test
    public void test005FilesInfo() {
        Map<String, Long> map = LogCT.getAllFilesInfo();
        if (map != null) {
            StringBuilder info = new StringBuilder();
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                info.append("文件日期：").append(entry.getKey()).append("  文件大小（bytes）：").append(
                        entry.getValue()).append("\n");
            }
            Log.d(TAG, info.toString());
        }
    }

    // Functions

    private String getTodayDate() {
        return mDateFormat.format(new Date(System.currentTimeMillis()));
    }

    private void assertWriteLog() throws InterruptedException {
        final int[] statusCode = new int[1];
        mLatch.await(2333, TimeUnit.MILLISECONDS);
        assertEquals("write状态码", LogCT.getLastErrorCode(), 0);
    }
}
