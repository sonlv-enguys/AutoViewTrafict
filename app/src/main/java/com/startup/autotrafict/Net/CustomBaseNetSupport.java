package com.startup.autotrafict.Net;

import android.util.Log;

import com.telpoo.frame.net.BaseNetSupport;
import com.telpoo.frame.net.NetConfig;
import com.telpoo.frame.object.BaseObject;
import com.telpoo.frame.utils.Mlog;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class CustomBaseNetSupport {
    protected static String TAG = CustomBaseNetSupport.class.getSimpleName();
    private String contentType;
    private String userAgent;
    private Integer connectTimeout;
    private Integer soTimeout;
    private String authorization;
    private int numberRetry = 3;
    // private volatile static BaseNetSupportBeta instance;
    private static CustomBaseNetSupport instance;

    public static CustomBaseNetSupport getInstance() {
        if (instance == null) {
            if (instance == null) {
                instance = new CustomBaseNetSupport();
            }
        }
        return instance;
    }

    private CustomBaseNetSupport() {

    }

    public void init(NetConfig netConfig) {
        connectTimeout = netConfig.getConnectTimeout();
        soTimeout = netConfig.getSoTimeout();
        authorization = netConfig.getAuthorization();
        contentType = netConfig.getContentType();
        userAgent = netConfig.getUserAgent();
        numberRetry = netConfig.getNumberRetry();
    }


    public String simplePutHttp(String url, String parram) {
        return methodHttp("PUT", url, parram);
    }

    private String methodHttp(String method, String url, String parram) {
        HttpURLConnection urlConnection = getConnection(method, url, parram);

        return getResponseFromHttpURLConnection(urlConnection);
    }

    private String getResponseFromHttpURLConnection(HttpURLConnection urlConnection) {
        if (urlConnection == null) {

            return null;
        }
        if (numberRetry == 0)
            return null;
        int retryCount = 0;
        do {
            try {
                int code = urlConnection.getResponseCode();
                Mlog.D("response code=" + code);
                boolean isError = code >= 400;
                InputStream in = null;
                in = isError ? urlConnection.getErrorStream() : urlConnection.getInputStream();
                String res = IOUtils.toString(in, "UTF-8");  //FileSupport.readFromInputStream(in);
                urlConnection.disconnect();
                return res;

            } catch (IOException e) {
                Mlog.E(TAG + " 983793 getResponseFromHttpURLConnection - " + e.getMessage());

            }

        } while (++retryCount < numberRetry);
        urlConnection.disconnect();
        return null;
    }

    private HttpURLConnection getConnection(String method, String url, String parram) {
        Mlog.D(TAG + "method=" + method + "  url=" + url + " parram=" + parram);
        HttpURLConnection conn = null;
        URL request_url = null;
        try {
            request_url = new URL(url);
        } catch (MalformedURLException e) {
            Mlog.E(TAG + " 920183 getConnection - url=" + url + "---" + e.getMessage());
            return null;
        }

        try {
            conn = (HttpURLConnection) request_url.openConnection();
        } catch (IOException e) {
            Mlog.E(TAG + " 79192301823 getConnection - " + e.getMessage());
            return null;
        }
        if (connectTimeout != null)
            conn.setConnectTimeout(connectTimeout);
        if (soTimeout != null)
            conn.setReadTimeout(soTimeout);
        if (authorization != null)
            conn.setRequestProperty("Authorization", authorization);
        if (contentType != null)
            conn.setRequestProperty("Content-Type", contentType);
        if (userAgent != null)
            conn.setRequestProperty("User-Agent", userAgent);


        if (parram != null) { // for POST / PUT
            conn.setDoOutput(true);
            try {
                conn.setRequestMethod(method);
            } catch (ProtocolException e) {
                Mlog.E(TAG + " 23818023 getConnection - " + e.getMessage());
                return null;
            }
            byte[] postDataBytes = new byte[0];
            try {
                postDataBytes = parram.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                Mlog.E(TAG + " 9193739 getConnection - " + e.getMessage());
                return null;
            }
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            try {
                conn.getOutputStream().write(postDataBytes);
            } catch (IOException e) {
                Mlog.E(TAG + " 0808129373 getConnection - " + e.getMessage());
                return null;
            }
        }

        return conn;


    }

    //----------------------------------------
    public final NetData simplePostJson(String baseUrl, BaseObject ojParram) {
        NetConfig.Builder builder = new NetConfig.Builder();
        builder.contentType("application/json");
        NetData data = methodPost(builder.build(), baseUrl, ojParram.toJson().toString());
        return data;
    }

    public final NetData simplePostJson(String baseUrl, JSONObject ojParram) {
        NetConfig.Builder builder = new NetConfig.Builder();
        builder.contentType("application/json");
        builder.connectTimeout(60000);
        NetData data = methodPost(builder.build(), baseUrl, ojParram.toString());
        return data;
    }

    public final NetData simplePostJsonObject(String baseUrl, BaseObject ojParram) {
        NetConfig.Builder builder = new NetConfig.Builder();
        builder.contentType("application/json");
        NetData data = methodPost(builder.build(), baseUrl, ojParram.get("param"));
        return data;
    }

    private final NetData methodPost(NetConfig netConfig, String url, String parram) {
        NetData data = new NetData();
        BaseNetSupport baseNetSupportBeta = BaseNetSupport.getInstance();
        baseNetSupportBeta.init(netConfig);
        String res = baseNetSupportBeta.simplePostHttp(url, parram);
        Log.d("telpoo", "POST: " + url);
        Log.d("telpoo", "parram: " + parram);
        Log.d("telpoo", "res: " + res);
        if (res == null) {
            data.setConnectError();
            return data;
        }
        data.setSuccess(res);
        return data;
    }

    //----------------------------------------
    public final NetData simplePutJson(String baseUrl, BaseObject ojParram) {
        NetConfig.Builder builder = new NetConfig.Builder();
        builder.contentType("application/json");
        NetData data = methodPut(builder.build(), baseUrl, ojParram.toJson().toString());
        return data;

    }

    private final NetData methodPut(NetConfig netConfig, String url, String parram) {
        NetData data = new NetData();
        BaseNetSupport baseNetSupportBeta = BaseNetSupport.getInstance();
        baseNetSupportBeta.init(netConfig);
        String res = simplePutHttp(url, parram);
        Log.d("telpoo", "PUT: " + url);
        Log.d("telpoo", "parram: " + parram);
        Log.d("telpoo", "res: " + res);
        if (res == null) {
            data.setConnectError();
            return data;
        }
        data.setSuccess(res);
        return data;
    }


    private final NetData methodDelete(NetConfig netConfig, String url, String parram) {
        NetData data = new NetData();
        BaseNetSupport baseNetSupportBeta = BaseNetSupport.getInstance();
        baseNetSupportBeta.init(netConfig);
        String res = simpleDelHttp(url, parram);
        Log.d("telpoo", "DELETE: " + url);
        Log.d("telpoo", "parram: " + parram);
        Log.d("telpoo", "res: " + res);
        if (res == null) {
            data.setConnectError();
            return data;
        }
        data.setSuccess(res);
        return data;
    }


    public String simpleDelHttp(String url, String parram) {
        return methodHttp("DELETE", url, parram);
    }

}