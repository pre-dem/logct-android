package qiniu.dem.logct;

final class WriteTask implements LogCTRunnable {
    private final String log; //日志

    private final boolean isMainThread;

    private final long threadId;

    private final String threadName;

    private final long localTime;

    private final int type;

    public WriteTask(String log, boolean isMainThread, long threadId, String threadName, long localTime, int type) {
        this.log = log;
        this.isMainThread = isMainThread;
        this.threadId = threadId;
        this.threadName = threadName;
        this.localTime = localTime;
        this.type = type;
    }

    @Override
    public void run(LogCTImpl l) {
        l.doWrite(log, isMainThread, threadId, threadName, localTime, type);
    }
}
