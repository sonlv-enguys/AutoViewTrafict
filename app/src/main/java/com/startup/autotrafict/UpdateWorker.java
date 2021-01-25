package com.startup.autotrafict;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class UpdateWorker extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SettingSupport.isStop(context)) {
            Log.d("SonLv", "UpdateSmsWorker isStop=true");
            return;
        }

        if (Calendar.getInstance().getTimeInMillis() - SettingSupport.getEndTimeProcess(context) < 3 * 60 * 1000) {
            Log.d("SonLv", "Đang chạy auto");
            return;
        }
        SettingSupport.setEndTimeProcess(context);

        Intent intent1 = new Intent(context, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("auto", true);
        context.startActivity(intent1);
        Log.d("SonLv", "UpdateSmsWorker: startActivity");

    }

    public static void start(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, UpdateWorker.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1 * 60 * 1000, alarmIntent);
        Log.d("SonLv", "UpdateSmsWorker: start");
    }


}
