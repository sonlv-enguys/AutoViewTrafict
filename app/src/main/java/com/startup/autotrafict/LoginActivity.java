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
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.startup.autotrafict.Net.TaskNet;
import com.startup.autotrafict.Net.TaskType;
import com.startup.autotrafict.Net.UserObj;
import com.telpoo.frame.model.BaseModel;
import com.telpoo.frame.net.BaseNetSupport;
import com.telpoo.frame.object.BaseObject;

import java.io.OutputStream;

public class LoginActivity extends AppCompatActivity {

    EditText edtUserName, edtPassword;
    View btnLogin;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        mProgressDialog = new ProgressDialog(LoginActivity.this);
        mProgressDialog.setCancelable(false);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        edtUserName.setText("shinglala@gmail.com");
        edtPassword.setText("Admin@123456789");
    }

    public void login() {
        final String username = edtUserName.getText().toString();
        String password = edtPassword.getText().toString();
        if (username.isEmpty() || password.isEmpty()) {
            showToast("Tên đăng nhập hoặc mật khẩu không được bỏ trống!");
            return;
        }
        BaseObject baseObject = new BaseObject();
        baseObject.set(UserObj.email, username);
        baseObject.set(UserObj.password, password);
        TaskNet taskNet = new TaskNet(new BaseModel() {
            @Override
            public void onSuccess(int taskType, Object data, String msg) {
                super.onSuccess(taskType, data, msg);
                closeProcessDialog();
                showToast("Thành công!");
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            @Override
            public void onFail(int taskType, String msg) {
                super.onFail(taskType, msg);
                showToast(msg);
                closeProcessDialog();
            }
        }, TaskType.TASK_LOGIN, getApplicationContext());
        taskNet.setTaskParram("parram", baseObject);
        taskNet.exe();
        showProcessDialog("Đang đăng nhập vào hệ thống...");
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


    public void showToast(final String sms) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LoginActivity.this, sms, Toast.LENGTH_SHORT).show();
            }
        });

    }


}
