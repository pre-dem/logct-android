package qiniu.dem.logct;

import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

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
    private final String path;
    private final UploadHandler mSendLogCallback;
    private final String mUploadLogUrl;
    private final String mDate;

    Uploader(UploadHandler handler, DeviceInfo info, String path, String url, String date) {
        this.path = path;
        mRequestHeaders = header(info);
        mSendLogCallback = handler;
        mUploadLogUrl = url;
        mDate = date;
    }

    private static void putNotEmpty(Map<String, String> m, String value, String key) {
        if (value != null && !value.equals("")) {
            m.put(key, value);
        }
    }


    private static String infoHeader(DeviceInfo info) {
        Map<String,String> dict = new HashMap<>();
        putNotEmpty(dict, info.userID, "user");
        putNotEmpty(dict, info.deviceID, "deviceId");
        putNotEmpty(dict, info.channel, "channel");
        putNotEmpty(dict, info.provider, "provider");
        putNotEmpty(dict, info.extra, "extra");

        dict.put("platform", "Android");
        dict.put("sdkVersion", LogCT.Version);
        dict.put("osVersion", SystemInfo.osVersion());
        dict.put("manufacturer", Build.MANUFACTURER.trim());
        dict.put("deviceType", SystemInfo.device());

        dict.put("bundleVersion", SystemInfo.appVersion);
        dict.put("appVersion", SystemInfo.appVersion);
        dict.put("appName", SystemInfo.appName);
        dict.put("packageId", SystemInfo.packageId);

        JSONObject o = new JSONObject(dict);
        String s = o.toString();
        byte[] encode = Base64.encode(s.getBytes(), Base64.NO_WRAP);
        return new String(encode);
    }

    private Map<String,String> header(DeviceInfo info){
        String infoStr = infoHeader(info);
        Map<String, String> h = new HashMap<>();
        h.put("X-REQINFO", infoStr);
        h.put("fileDate", mDate);
        h.put("Content-Type", "binary/octet-stream");
        return h;
    }

    public void sendLog(File logFile) {
        sendFile(logFile, mRequestHeaders, mUploadLogUrl);
        // Must Call finish after send log
        mSendLogCallback.complete(ErrorCode.OK, null);
        if (logFile.getName().contains(".copy")) {
            logFile.delete();
        }
    }

    private void sendFile(File logFile, Map<String, String> headers, String url) {
        try {
            FileInputStream fileStream = new FileInputStream(logFile);
            doPostRequest(url, fileStream, headers);
        } catch (IOException e) {
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
        if (data == null) {
            data = new byte[0];
        }
        mSendLogCallback.complete(statusCode, new String(data));
    }

    @Override
    public void run() {
        File file = new File(path);
        sendLog(file);
    }
}
