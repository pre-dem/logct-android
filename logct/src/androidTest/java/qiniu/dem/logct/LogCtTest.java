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
        Config config = new Config.Builder("", "")
                .setCachePath(cachePath)
                .setPath(path)
                .build();
        LogCT.init(config, null);
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
                SystemInfo.out.println("today " + code);
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
        return mDateFormat.format(new Date(SystemInfo.currentTimeMillis()));
    }

    private void assertWriteLog() throws InterruptedException {
        final int[] statusCode = new int[1];
        mLatch.await(2333, TimeUnit.MILLISECONDS);
        assertEquals("write状态码", LogCT.getLastErrorCode(), 0);
    }
}
