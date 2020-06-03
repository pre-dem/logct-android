package qiniu.dem.logct;

public interface UploadHandler {
    void complete(int code, String data);
}
