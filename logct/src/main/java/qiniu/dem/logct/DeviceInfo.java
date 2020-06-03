package qiniu.dem.logct;

public final class DeviceInfo {
    /**
     * user ID
     */
    public final String userID;

    /**
     * device ID
     */
    public final String deviceID;

    /**
     * userID 来源，比如wechat, weibo
     */
    public final String provider;

    /**
     * app 下载渠道
     */
    public final String channel;

    /**
     * 额外的信息，如果有多个自定义字段，需要处理成一个字符串，之后自行解析
     */
    public final String extra;

    public DeviceInfo(String userID, String deviceID, String provider, String channel, String extra) {
        this.userID = userID;
        this.deviceID = deviceID;
        this.provider = provider;
        this.channel = channel;
        this.extra = extra;
    }

    public DeviceInfo(String userID, String deviceID) {
        this(userID, deviceID, "", "", "");
    }
}
