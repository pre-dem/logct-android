package qiniu.dem.logct;

import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

final class Uploader implements Runnable {
    private static final String TAG = "Uploader";

    private final Map<String, String> mRequestHeaders;
    private final DeviceInfo info;
    private final String path;
    private final UploadHandler mSendLogCallback;
    private String mUploadLogUrl;

    Uploader(UploadHandler handler, DeviceInfo info, String path) {
        this.info = info;
        this.path = path;
        mRequestHeaders = new HashMap<>();
        mSendLogCallback = handler;
    }

    public void sendLog(File logFile) {
        doSendFileByAction(logFile, mRequestHeaders, mUploadLogUrl);
        // Must Call finish after send log
        mSendLogCallback.complete(ErrorCode.OK, null);
        if (logFile.getName().contains(".copy")) {
            logFile.delete();
        }
    }

    /**
     * set upload log url.
     *
     * @param uploadLogUrl
     */
    public void setUrl(String uploadLogUrl) {
        mUploadLogUrl = uploadLogUrl;
    }

    /**
     * set request header.
     *
     * @param headers
     */
    public void setRequestHeader(Map<String, String> headers) {
        mRequestHeaders.clear();
        if (headers != null) {
            mRequestHeaders.putAll(headers);
        }
    }

    private void doSendFileByAction(File logFile, Map<String, String> headers, String url) {
        try {
            FileInputStream fileStream = new FileInputStream(logFile);
            doPostRequest(url, fileStream, headers);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doPostRequest(String url, InputStream inputData, Map<String, String> headerMap) {
        byte[] data = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        HttpURLConnection c = null;
        ByteArrayOutputStream back;
        byte[] buffer = new byte[2048];
        int statusCode = -1;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            if (c instanceof HttpsURLConnection) {
                ((HttpsURLConnection) c).setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            Set<Map.Entry<String, String>> entrySet = headerMap.entrySet();
            for (Map.Entry<String, String> tempEntry : entrySet) {
                c.addRequestProperty(tempEntry.getKey(), tempEntry.getValue());
            }
            c.setReadTimeout(15000);
            c.setConnectTimeout(15000);
            c.setDoInput(true);
            c.setDoOutput(true);
            c.setRequestMethod("POST");
            outputStream = c.getOutputStream();
            int i;
            final ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] tmp = null;
            try {
                while ((i = inputData.read(buffer)) != -1) {
                    out.write(buffer, 0, i);
                }
                tmp = out.toByteArray();
            } finally {
                out.close();
            }
            outputStream.write(tmp);
            outputStream.flush();
            statusCode = c.getResponseCode();
            if (statusCode / 100 == 2) {
                back = new ByteArrayOutputStream();
                inputStream = c.getInputStream();
                while ((i = inputStream.read(buffer)) != -1) {
                    back.write(buffer, 0, i);
                }
                data = back.toByteArray();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputData != null) {
                try {
                    inputData.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (c != null) {
                c.disconnect();
            }
        }
        Log.d(TAG, "log send completed, http statusCode : " + statusCode);
        mSendLogCallback.complete(statusCode, new String(data));
    }

    @Override
    public void run() {
        File file = new File(path);
        sendLog(file);
    }
}
