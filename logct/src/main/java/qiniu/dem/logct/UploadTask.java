package qiniu.dem.logct;

class UploadTask implements LogCTRunnable {

    private final String date;
    UploadHandler handler;

    UploadTask(String date, UploadHandler h) {
        this.date = date;
        handler = h;
    }

    @Override
    public void run(LogCTImpl l) {
        l.doSend(date, handler);
    }
}
