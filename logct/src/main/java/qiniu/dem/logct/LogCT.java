package qiniu.dem.logct;

import java.util.Map;

public final class LogCT {

    private static LogCTImpl instance = new LogCTImpl();

    public static int init(Config config) {
        return instance.init(config);
    }

    public static void setDeviceInfo(DeviceInfo info) {
        instance.setInfo(info);
    }


    /**
     * 存储一条日志，将来通过Upload上传
     *
     * @param type 日志类型
     * @param log  日志字符串
     * @brief 用例：
     * w(1, @"this is a test");
     */
    public static void w(String log, int type) {
        instance.write(log, type);
    }

    /**
     * @brief 立即写入日志文件
     */
    public static void flush() {
        instance.flush();
    }

    /**
     * @param date    日期数组，格式：“2018-07-27”
     * @param handler 发送回调
     * @brief 发送日志
     */
    public static void upload(String date, UploadHandler handler) {
        instance.upload(date, handler);
    }

    /**
     * @brief 返回所有日志文件信息
     */
    public static Map<String, Long> getAllFilesInfo() {

        return instance.getAllFilesInfo();
    }

    /**
     * @brief Debug Log开关
     */
    public static void setDebug(boolean debug) {
        LogCTImpl.mDebug = debug;
    }

    public static int getLastErrorCode() {
        return instance.lastErrorCode;
    }
}
