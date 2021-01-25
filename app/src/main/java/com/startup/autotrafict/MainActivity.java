package com.startup.autotrafict;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.telpoo.frame.net.BaseNetSupport;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    View btnRunAuto, btnStopAuto, btnLogout;
    AlertDialog alertDialog;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SettingSupport.getToken(this).isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        UpdateWorker.start(getApplicationContext());
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);

        btnStopAuto = findViewById(R.id.btnStopAuto);
        btnRunAuto = findViewById(R.id.btnRunAuto);
        btnLogout = findViewById(R.id.btnLogout);

        btnRunAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingSupport.isStop(MainActivity.this, false);
                callAutolView();
            }
        });

        btnStopAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingSupport.isStop(MainActivity.this, true);
                Log.d("SonLv", "Đã dừng quá trình auto");
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("Bạn đang đăng xuất khỏi hệ thống!");
            }
        });

        setTitle(getString(R.string.app_name) + "  " + BuildConfig.VERSION_NAME);

        if (getIntent().getBooleanExtra("stop_auto", false)) {
            SettingSupport.isStop(MainActivity.this, true);
            closeNotification();
            Log.d("SonLv", "Đã dừng quá trình auto");
            return;
        }

        showNotification();

        if (!BaseNetSupport.isNetworkAvailable(this)) {
            onMobileData();
            return;
        }

        if (getIntent().getBooleanExtra("auto", false)) callAutolView();

        String sms = getIntent().getStringExtra("sms");
        if (sms != null) showAlertDialog("Thông báo", sms);

    }

    public void onMobileData() {
        Log.d("SonLv", "OnMobileData");
        try {
            OutputStream out = new ProcessBuilder(new String[0]).redirectErrorStream(true).command(new String[]{"su"}).start().getOutputStream();
            out.write("settings put global mobile_data 1\n".getBytes("UTF-8"));
            out.flush();
            out.close();
            Log.d("SonLv", "Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String title, String sms) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(sms);
        builder.setCancelable(false);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {

        }
    }

    public void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo");
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SettingSupport.saveToken(MainActivity.this, "");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception e) {

        }
    }


    public void closeProcessDialog() {
        if (isFinishing()) return;
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
        } catch (Exception e) {

        }
    }

    public void showProcessDialog(String sms) {
        if (isFinishing()) return;
        try {
            if (mProgressDialog != null) mProgressDialog.setMessage(sms);
            if (mProgressDialog != null && !mProgressDialog.isShowing()) mProgressDialog.show();
        } catch (Exception e) {

        }
    }

    public void callAutolView() {
        if (SettingSupport.isStop(this)) {
            Log.d("SonLv", "isStop ");
            return;
        }
        try {
            OutputStream out = new ProcessBuilder(new String[0]).redirectErrorStream(true).command(new String[]{"su"}).start().getOutputStream();
            out.write("am instrument -w -r   -e debug false -e class 'com.startup.autotrafict.AutoView' com.startup.autotrafict.test/androidx.test.runner.AndroidJUnitRunner\n".getBytes("UTF-8"));
            out.flush();
            out.close();
            showToast("Bắt đầu thực hiện Auto");
        } catch (Exception e) {
            Log.d("SonLv", "Exception: " + e.getMessage());
            showToast("Có lỗi khi Auto");
        }
    }


    public void showToast(final String sms) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, sms, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showNotification() {
        try {
            String CHANNEL_ID = "my_channel_01";// The id of the channel.
            CharSequence name = "sonlv";// The user-visible name of the channel.
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotificationManager.createNotificationChannel(mChannel);
            }

            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("stop_auto", true);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(MainActivity.this)
                    .setContentTitle("Auto Report Youtobe")
                    .setContentText("Click để dừng quá trình auto!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(resultPendingIntent)
                    .build();


            mNotificationManager.notify(1, notification);
        } catch (Exception e) {
            Log.d("SonLv", "Exception: " + e.getMessage());
        }
    }

    public void reboot() {
        try {
            OutputStream out = new ProcessBuilder(new String[0]).redirectErrorStream(true).command(new String[]{"su", "-c", "reboot"}).start().getOutputStream();
            out.flush();
            out.close();
            showToast("Bắt đầu thực hiện Auto");
        } catch (Exception e) {
            Log.d("SonLv", "Exception: " + e.getMessage());
            showToast("Có lỗi khi Auto");
        }
    }


    public void closeNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }

}
