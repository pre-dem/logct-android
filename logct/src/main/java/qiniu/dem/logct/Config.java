package qiniu.dem.logct;

import android.text.TextUtils;

public class Config {

    public static final int AppKeyLength = 24;
    private static final long DAYS = 24 * 60 * 60 * 1000; //天
    private static final int M = 1024 * 1024; //M
    private static final long DEFAULT_DAY = 7 * DAYS; //默认删除天数
    private static final int DEFAULT_FILE_SIZE = 10 * M;
    private static final long DEFAULT_MIN_SDCARD_SIZE = 50 * M; //最小的SD卡小于这个大小不写入
    private static final int DEFAULT_QUEUE = 500;
    String mCachePath; //mmap缓存路径
    String mPathPath; //file文件路径

    int mMaxFile = DEFAULT_FILE_SIZE; //删除文件最大值
    long mDay = DEFAULT_DAY; //删除天数
    long mMaxQueue = DEFAULT_QUEUE;
    long mMinSDCard = DEFAULT_MIN_SDCARD_SIZE; //最小sdk卡大小

    String mEncryptKey16; //128位aes加密Key
    String mEncryptIv16; //128位aes加密IV

    String mServer;
    String mAppId;

    private Config() {

    }

    boolean isValid() {
        boolean valid = false;
        if (!TextUtils.isEmpty(mCachePath) && !TextUtils.isEmpty(mPathPath) && mEncryptKey16 != null
                && mEncryptIv16 != null) {
            valid = true;
        }
        return valid;
    }

    private void setCachePath(String cachePath) {
        mCachePath = cachePath;
    }

    private void setPathPath(String pathPath) {
        mPathPath = pathPath;
    }

    private void setMaxFile(int maxFile) {
        mMaxFile = maxFile;
    }

    private void setDay(long day) {
        mDay = day;
    }

    private void setMinSDCard(long minSDCard) {
        mMinSDCard = minSDCard;
    }

    private void setEncryptKey16(String encryptKey16) {
        mEncryptKey16 = encryptKey16;
    }

    private void setEncryptIV16(String encryptIv16) {
        mEncryptIv16 = encryptIv16;
    }

    public static final class Builder {
        String mCachePath; //mmap缓存路径
        String mPath; //file文件路径
        int mMaxFile = DEFAULT_FILE_SIZE; //删除文件最大值
        long mDay = DEFAULT_DAY; //删除天数
        long mMinSDCard = DEFAULT_MIN_SDCARD_SIZE;
        String mAppKey;
        String mServer;

        public Builder(String server, String appKey) {
            mAppKey = appKey;
            mServer = server;
        }

        public Builder setCachePath(String cachePath) {
            mCachePath = cachePath;
            return this;
        }

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setMaxFile(int maxFile) {
            mMaxFile = maxFile * M;
            return this;
        }

        public Builder setDay(long day) {
            mDay = day * DAYS;
            return this;
        }

        public Builder setMinSDCard(long minSDCard) {
            this.mMinSDCard = minSDCard;
            return this;
        }


        public Config build() {
            if (mServer == null) {
                throw new IllegalArgumentException("need server");
            }
            if (mAppKey == null || mAppKey.length() != AppKeyLength) {
                throw new IllegalArgumentException("appkey is not valid");
            }
            Config config = new Config();
            config.setCachePath(mCachePath);
            config.setPathPath(mPath);
            config.setMaxFile(mMaxFile);
            config.setMinSDCard(mMinSDCard);
            config.setDay(mDay);


            if (!mServer.startsWith("http://") && !mServer.startsWith("https://")) {
                mServer = "http://" + mServer;
            }

            String aesKey = mAppKey.substring(8);
            String aesIv = mAppKey.substring(0, 16);
            String appId = mAppKey.substring(0, 8);
            config.setEncryptKey16(aesKey);
            config.setEncryptIV16(aesIv);
            config.mAppId = appId;
            config.mServer = mServer;

            return config;
        }
    }
}