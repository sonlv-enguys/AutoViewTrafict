package com.startup.autotrafict.Net;

import android.content.Context;
import android.util.Log;

import com.startup.autotrafict.LoginActivity;
import com.startup.autotrafict.SettingSupport;
import com.startup.autotrafict.db.DbSupport;
import com.telpoo.frame.net.BaseNetSupport;
import com.telpoo.frame.net.NetConfig;
import com.telpoo.frame.object.BaseObject;
import com.telpoo.frame.utils.JsonSupport;

import org.json.JSONObject;

import java.util.ArrayList;

public class NetSupport {

    private static final NetData simpleGet(String url) {
        BaseNetSupport baseNetSupport = BaseNetSupport.getInstance();
        NetConfig.Builder builder = new NetConfig.Builder();
        baseNetSupport.init(builder.build());
        Log.d("dataApi", "url: " + url);
        String res = baseNetSupport.method_GET(url);
        Log.d("dataApi", "Respone: " + res);
        NetData data = new NetData();
        if (res == null) {
            data.setConnectError();
            return data;
        }

        try {
            JSONObject resJson = new JSONObject(res);
            if (resJson.has("data")) data.setSuccess(resJson.getString("data"));
            else data.setSuccess(res);
        } catch (Exception e) {

        }

        return data;
    }

    private static final NetData simplePost(String url, String parram) {
        BaseNetSupport baseNetSupport = BaseNetSupport.getInstance();
        NetConfig.Builder builder = new NetConfig.Builder();
        baseNetSupport.init(builder.build());
        Log.d("dataApi", "url: " + url);
        Log.d("dataApi", "parram: " + parram);
        String res = baseNetSupport.simplePostHttp(url, parram);

        Log.d("dataApi", "Respone: " + res);
        NetData data = new NetData();
        if (res == null) {
            data.setConnectError();
            return data;
        }
        data.setSuccess(res);

        return data;
    }

    private static final NetData simplePostJson(String url, BaseObject object) {
        CustomBaseNetSupport baseNetSupport = CustomBaseNetSupport.getInstance();
        NetConfig.Builder builder = new NetConfig.Builder();
        baseNetSupport.init(builder.build());
        Log.d("dataApi", "url: " + url);
        Log.d("dataApi", "parram: " + object.toJson());
        NetData data = baseNetSupport.simplePostJson(url, object);
        String res = data.getDataAsString();

        Log.d("dataApi", "Respone: " + res);
        if (res == null) {
            data.setConnectError();
            return data;
        }
        data.setSuccess(res);

        return data;
    }


    public static String addParramToUrl(String url, BaseObject object) {
        if (object == null) return url;

        return url + "?" + object.convert2NetParrams();
    }


    public static NetData getTasks(Context context) {
        BaseObject baseObject = new BaseObject();
        baseObject.set("email", SettingSupport.getEmail(context));
        baseObject.set("token", SettingSupport.getToken(context));
        NetData data = simplePost(MyUrl.tasks, baseObject.convert2NetParrams());

        if (data.getcode() != 1) return data;
        try {
            JSONObject object=new JSONObject(data.getDataAsString());
            if (!object.optBoolean(ScripObj.status,false)){
                data.setMessage(NetData.CODE_FALSE, "Không có dữ liệu auto!");
                return data;
            }
            data.setSuccess(JsonSupport.jsonObject2BaseOj(object.getJSONObject("response")));
        } catch (Exception e) {
            e.printStackTrace();
            data.setMessage(NetData.CODE_FALSE, "Không lấy được dữ liệu auto!");

        }
        return data;
    }

    public static void sleep() {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }
    }

    public static NetData login(Context context, BaseObject object) {
        NetData data = simpleGet(MyUrl.get_token);
        String res = data.getDataAsString();
        if (data.getcode() != 1) return data;
        try {
            if (res == null) {
                data.setMessage(NetData.CODE_FALSE, "Không lấy được token");
                return data;
            }

            JSONObject objectToken = new JSONObject(res);
            String token = objectToken.getString("access_token");
            object.set("token", token);

            data = simplePost(MyUrl.login, object.convert2NetParrams());
            JSONObject objectResult = new JSONObject(data.getDataAsString()).getJSONObject("result");
            if (objectResult.optString("status", "").equals("success")) {
                data.setSuccess(objectResult);
                SettingSupport.saveToken(context, token);
                SettingSupport.saveEmail(context, object.get("email", ""));
            } else
                data.setMessage(NetData.CODE_FALSE, objectResult.optString("message", "Có lỗi đăng nhập, vui lòng kiểm tra lại!"));
        } catch (Exception e) {
            e.printStackTrace();
            data.setCodeErrorServer();
        }
        return data;
    }

}