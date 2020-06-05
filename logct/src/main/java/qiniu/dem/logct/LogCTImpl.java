package qiniu.dem.logct;

import android.content.Context;
import android.os.Looper;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

final class LogCTImpl implements Runnable {
    private static final String TAG = "LogCTImpl";
    private static final int CACHE_SIZE = 8 * 1024;
    private static final int MINUTE = 60 * 1000;
    private static final long LONG = 24 * 60 * 60 * 1000;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static boolean mDebug = false;
    private final Logan logan;
    private final Object sync = new Object();
    private final Object sendSync = new Object();
    volatile int lastErrorCode;
    private Thread thread;
    private ConcurrentLinkedQueue<LogCTRunnable> mTaskQueue = new ConcurrentLinkedQueue<>();
    private String mCachePath; // 缓存文件路径
    private String mPath; //文件路径
    private long mSaveTime; //存储时间
    private int mMaxLogFile;//最大文件大小
    private long mMinSDCard;
    private long mMaxQueue; //最大队列数
    private String mEncryptKey16;
    private String mEncryptIv16;
    private String mAppID;
    private String mServer;
    private DeviceInfo mInfo;
    private volatile boolean mIsRun = true;
    private long mCurrentDay;
    private boolean mIsWorking;
    private File mFileDirectory;
    private boolean mIsSDCard;
    private long mLastTime;
    private ExecutorService mSingleThreadExecutor;
    private ConcurrentLinkedQueue<LogCTRunnable> mSendQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isSending;

    LogCTImpl() {
        logan = new Logan();
        thread = new Thread(this, "logct");
    }

    public static long getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        long tempTime = 0;
        try {
            String dataStr = dateFormat.format(new Date(currentTime));
            tempTime = dateFormat.parse(dataStr).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempTime;
    }

    public static String getDateStr(long time) {
        return dateFormat.format(new Date(time));
    }

    int init(Config config, Context context) {
        mPath = config.mPathPath;
        mCachePath = config.mCachePath;
        mSaveTime = config.mDay;
        mMinSDCard = config.mMinSDCard;
        mMaxLogFile = config.mMaxFile;
        mMaxQueue = config.mMaxQueue;
        mEncryptKey16 = config.mEncryptKey16;
        mEncryptIv16 = config.mEncryptIv16;
        mAppID = config.mAppId;
        mServer = config.mServer;
        Log.d(TAG, mServer);
        mTaskQueue.add(new LogCTRunnable() {
            @Override
            public void run(LogCTImpl l) {
                doInit();
            }
        });
        thread.start();
        return ErrorCode.OK;
    }

    void doInit() {
        logan.init(mCachePath, mPath, mMaxLogFile, mEncryptKey16, mEncryptIv16);
    }

    int write(String log, int type) {
        if (TextUtils.isEmpty(log)) {
            return ErrorCode.OK;
        }
        String threadName = Thread.currentThread().getName();
        long threadId = Thread.currentThread().getId();
        boolean isMain = (Looper.getMainLooper() == Looper.myLooper());
        long localTime = System.currentTimeMillis();

        mTaskQueue.add(new WriteTask(log, isMain, threadId, threadName, localTime, type));
        notifyRun();
        return ErrorCode.OK;
    }

    void flush() {
        mTaskQueue.add(new LogCTRunnable() {
            @Override
            public void run(LogCTImpl l) {
                doFlush();
            }
        });
        notifyRun();
    }

    void upload(final String date, final UploadHandler handler) {
        mTaskQueue.add(new UploadTask(date, handler));
        notifyRun();
    }

    Map<String, Long> getAllFilesInfo() {

        File dir = getDir();
        if (!dir.exists()) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }
        Map<String, Long> allFilesInfo = new HashMap<>();
        for (File file : files) {
            try {
                allFilesInfo.put(getDateStr(Long.parseLong(file.getName())), file.length());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return allFilesInfo;
    }

    File getDir() {
        return new File(mPath);
    }

    private long getDateTime(String time) {
        long tempTime = 0;
        try {
            tempTime = dateFormat.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return tempTime;
    }

    @Override
    public void run() {
        while (mIsRun) {
            synchronized (sync) {
                mIsWorking = true;
                try {
                    LogCTRunnable task = mTaskQueue.poll();
                    if (task == null) {
                        mIsWorking = false;
                        sync.wait();
                        mIsWorking = true;
                    } else {
                        task.run(this);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    mIsWorking = false;
                }
            }
        }
    }

    void notifyRun() {
        if (!mIsWorking) {
            synchronized (sync) {
                sync.notify();
            }
        }
    }

    void quit() {
        mIsRun = false;
        if (!mIsWorking) {
            synchronized (sync) {
                sync.notify();
            }
        }
    }

    public void setInfo(DeviceInfo info) {
        this.mInfo = info;
    }

    private boolean isDay() {
        long currentTime = System.currentTimeMillis();
        return mCurrentDay < currentTime && mCurrentDay + LONG > currentTime;
    }

    private void deleteExpiredFile(long deleteTime) {
        File dir = new File(mPath);
        if (dir.isDirectory()) {
            String[] files = dir.list();
            if (files != null) {
                for (String item : files) {
                    try {
                        if (TextUtils.isEmpty(item)) {
                            continue;
                        }
                        String[] longStrArray = item.split("\\.");
                        if (longStrArray.length > 0) {  //小于时间就删除
                            long longItem = Long.valueOf(longStrArray[0]);
                            if (longItem <= deleteTime && longStrArray.length == 1) {
                                new File(mPath, item).delete(); //删除文件
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void doFlush() {
        if (mDebug) {
            Log.d(TAG, "LogCT flush start");
        }
        logan.flush();
    }

    void doWrite(String log, boolean isMainThread, long threadId, String threadName, long localTime, int type) {
        if (mDebug) {
            Log.d(TAG, "LogCT write start");
        }
        if (mFileDirectory == null) {
            mFileDirectory = new File(mPath);
        }

        if (!isDay()) {
            long tempCurrentDay = getCurrentTime();
            //save时间
            long deleteTime = tempCurrentDay - mSaveTime;
            deleteExpiredFile(deleteTime);
            mCurrentDay = tempCurrentDay;
            logan.open(String.valueOf(mCurrentDay));
        }

        long currentTime = System.currentTimeMillis(); //每隔1分钟判断一次
        if (currentTime - mLastTime > MINUTE) {
            mIsSDCard = isCanWriteSDCard();
        }
        mLastTime = System.currentTimeMillis();

        if (!mIsSDCard) { //如果大于50M 不让再次写入
            return;
        }
        logan.write(type, log, localTime, threadName, threadId, isMainThread);
    }

    private String buildUrl(){
        return String.format("%s/logct/v1/native/%s/tasks", mServer, mAppID);
    }

    void doSend(String date, final UploadHandler handler) {
        if (mDebug) {
            Log.d(TAG, "LogCT send start");
        }
        long time = getDateTime(date);
        String name = String.valueOf(time);
        String path = prepareLogFile(name);
        if (path == null) {
            if (mDebug) {
                Log.d(TAG, "LogCT prepare log file failed, can't find log file");
            }
            return;
        }

        if (mSingleThreadExecutor == null) {
            mSingleThreadExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    // Just rename Thread
                    Thread t = new Thread(Thread.currentThread().getThreadGroup(), r,
                            "logct-thread-send-log", 0);
                    if (t.isDaemon()) {
                        t.setDaemon(false);
                    }
                    if (t.getPriority() != Thread.NORM_PRIORITY) {
                        t.setPriority(Thread.NORM_PRIORITY);
                    }
                    return t;
                }
            });
        }

        if (!isSending) {
            isSending = true;

            mSingleThreadExecutor.execute(new Uploader(new UploadHandler() {
                @Override
                public void complete(int code, String data) {
                    isSending = false;
                    mTaskQueue.addAll(mSendQueue);
                    mSendQueue.clear();
                    notifyRun();
                    handler.complete(code, data);
                }
            }, mInfo, path, buildUrl(), date));
        } else {
            mSendQueue.add(new UploadTask(date, handler));
        }
    }

    /**
     * 发送日志前的预处理操作
     */
    private String prepareLogFile(String date) {
        if (mDebug) {
            Log.d(TAG, "prepare log file " + date);
        }
        if (isFile(date)) { //是否有日期文件
            String src = mPath + File.separator + date;
            if (date.equals(String.valueOf(getCurrentTime()))) {
                doFlush();
                String des = mPath + File.separator + date + ".copy";
                if (copyFile(src, des)) {
                    return des;
                }
            } else {
                return src;
            }
        }
        return null;
    }

    private boolean copyFile(String src, String des) {
        boolean back = false;
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(new File(src));
            outputStream = new FileOutputStream(new File(des));
            byte[] buffer = new byte[CACHE_SIZE];
            int i;
            while ((i = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, i);
                outputStream.flush();
            }
            back = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return back;
    }

    private boolean isCanWriteSDCard() {
        boolean item = false;
        try {
            StatFs stat = new StatFs(mPath);
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            long total = availableBlocks * blockSize;
            if (total > mMinSDCard) { //判断SDK卡
                item = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return item;
    }

    private boolean isFile(String name) {
        boolean isExist = false;
        if (TextUtils.isEmpty(mPath)) {
            return false;
        }
        File file = new File(mPath + File.separator + name);
        if (file.exists() && file.isFile()) {
            isExist = true;
        }
        return isExist;
    }
}
