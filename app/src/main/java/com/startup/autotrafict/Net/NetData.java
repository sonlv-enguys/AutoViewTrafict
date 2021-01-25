package com.startup.autotrafict.Net;


import com.telpoo.frame.net.BaseNetSupport;

public class NetData {
    public static final int CODE_ERROR_SERVER = -2;
    public static final int CODE_ERROR_NETWORK = -1;
    public static final int CODE_SUCCESS = 1;
    public static final int CODE_FALSE = 2;
    int code = CODE_SUCCESS;
    String msg = "error(101)";
    Object data;

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        if (code == CODE_ERROR_NETWORK) msg = "Network not connected!";
        if (code == CODE_ERROR_SERVER) msg = "Connect to server error!";

        return msg;
    }

    public void setMessage(String msg) {

        this.msg = msg;
    }

    public void setMessage(int code,String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getcode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setCode(String strcode) {
        try {
            code = Integer.parseInt(strcode);
        } catch (NumberFormatException e) {
            //Log.e("")
        }
    }

    public void setConnectError() {
        String res = BaseNetSupport.getInstance().method_GET("https://www.google.com");
        if (res == null) code = CODE_ERROR_NETWORK;
        else code = CODE_ERROR_SERVER;
    }

    public void setCodeErrorServer() {
        code = NetData.CODE_ERROR_SERVER;
    }

    public void setSuccess() {
        code = CODE_SUCCESS;
    }

    public void setSuccess(Object data) {
        code = CODE_SUCCESS;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public String getDataAsString() {
        if (data == null) return null;
        return data.toString();
    }

    public boolean isSuccess() {
        if (code == CODE_SUCCESS) return true;
        return false;
    }
}
