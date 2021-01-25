package com.startup.autotrafict;

import android.content.Context;

import com.telpoo.frame.utils.SPRSupport;

import java.util.Calendar;

public class SettingSupport {
    private static final String isStop = "isStop";
    private static final String EndTimeProcess = "EndTimeProcess";
    private static final String token = "token";
    private static final String email = "email";

    public static long getEndTimeProcess(Context context) {
        return SPRSupport.getLong(EndTimeProcess, context, Calendar.getInstance().getTimeInMillis());
    }

    public static void setEndTimeProcess(Context context) {
        Calendar calendar = Calendar.getInstance();
        SPRSupport.save(EndTimeProcess, calendar.getTimeInMillis(), context);
    }

    public static void setEndTimeProcess(Context context, long time) {
        Calendar calendar = Calendar.getInstance();
        SPRSupport.save(EndTimeProcess, time, context);
    }

    public static boolean isStop(Context context) {
        return SPRSupport.getBool(isStop, context, false);
    }

    public static void isStop(Context context, boolean value) {
        SPRSupport.save(isStop, value, context);
    }

    public static String getToken(Context context) {
        return SPRSupport.getString(token, context, "");
    }

    public static void saveToken(Context context, String value) {
        SPRSupport.save(token, value, context);
    }

    public static String getEmail(Context context) {
        return SPRSupport.getString(email, context, "");
    }

    public static void saveEmail(Context context, String value) {
        SPRSupport.save(email, value, context);
    }


}
