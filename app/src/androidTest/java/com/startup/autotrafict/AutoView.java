/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.startup.autotrafict;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;

import com.startup.autotrafict.Net.ScripObj;
import com.startup.autotrafict.Net.TaskNet;
import com.startup.autotrafict.Net.TaskType;
import com.telpoo.frame.model.BaseModel;
import com.telpoo.frame.object.BaseObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

/**
 * Basic sample for unbundled UiAutomator.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class AutoView {
    String TAG = "SonLv";
    private UiDevice mDevice;

    long timeStart = 0;
    long timeLoading = 0;
    int step = 0;
    boolean isFinish = false;
    boolean isLoadedData = false;
    BaseObject objectScrip = new BaseObject();

    int dHeight = 0;
    int dWidth = 0;
    int page = 0;
    int countLoadData = 0;

    int startX = dWidth / 4;
    int startY = dHeight / 4;
    int endX = startX * 3;
    int endY = startY * 3;

    @Before
    public void startMainActivityFromHomeScreen() {
        mDevice = UiDevice.getInstance(getInstrumentation());
        timeStart = Calendar.getInstance().getTimeInMillis();
        timeLoading = Calendar.getInstance().getTimeInMillis();
        try {
            mDevice.wakeUp();
            Log.d("SonLv", "wakeUp");
        } catch (Exception e) {
            Log.d("SonLv", "Exception: " + e.getMessage());
        }
        dHeight = mDevice.getDisplayHeight();
        dWidth = mDevice.getDisplayWidth();

        startY = dHeight / 2;
        endY = startY + 10;
        startX = dWidth / 2;
        endX = startX + 5;
    }

    @Test
    public void runTesst() {
        if (isFinish || SettingSupport.isStop(getApplicationContext())) {
            Log.d(TAG, "Kết thúc quá trình auto!");
            return;
        }
        clearDataChrome();
        resetMobileData();
        getData();
        autoView();
    }

    public void autoView() {
        Log.d("SonLv", "step: " + step);
        if (isFinish || SettingSupport.isStop(getApplicationContext())) {
            Log.d(TAG, "Kết thúc quá trình auto!");
            return;
        }

        if (page > objectScrip.getInt(ScripObj.limit_page, 20)) {
            oppenMainActivity("Hết số lượng trang tìm kiếm");
            return;
        }

        try {
            mDevice.wakeUp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        SettingSupport.setEndTimeProcess(getApplicationContext());

        if (!isLoadedData) {
            Log.d("SonLv", "Đang lấy dữ liệu");
            sleep(2000);
            autoView();
            return;
        }
        checkClick();

        switch (step) {
            case 0:
                Log.d(TAG, "data: " + objectScrip.toJson());
                String url = "https://www.google.com/search?q= " + objectScrip.get(ScripObj.keyword, "");
                Log.d(TAG, "open: " + url);
                try {
                    Intent i = new Intent("android.intent.action.MAIN");
                    i.setComponent(ComponentName.unflattenFromString("com.android.chrome/com.android.chrome.Main"));
                    i.addCategory("android.intent.category.LAUNCHER");
                    i.setData(Uri.parse(url));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                } catch (ActivityNotFoundException e) {
                    // Chrome is not installed
                    e.printStackTrace();
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(i);
                }
                step = 1;
                break;
            case 1:
                UiObject2 item = mDevice.findObject(By.textContains(objectScrip.get(ScripObj.domain, "")));
                if (item != null) {
                    Log.d(TAG, "Đã tìm thấy domain: " + item.getText());
                    item.click();
                    step = 2;
                    break;
                }
                mDevice.swipe(startX, startY, endX, endX, 10);
                Log.d(TAG, "Scroll xuống để tìm domain");
                sleep(objectScrip.getInt(ScripObj.delay_step, 30) * 1000);
                break;
            case 2:
                Log.d(TAG, "Đang load site: " + objectScrip.get(ScripObj.domain, ""));
                UiObject2 itemUrl = mDevice.findObject(By.res("com.android.chrome:id/url_bar"));
                if (itemUrl == null) break;

                if (itemUrl.getText().contains(objectScrip.get(ScripObj.domain, ""))) {
                    int countScroll = 6;
                    int timeOut = objectScrip.getInt(ScripObj.delay_out, 60) / countScroll;
                    for (int i = 0; i < countScroll; i++) {
                        try {
                            UiScrollable appViews1 = new UiScrollable(new UiSelector().scrollable(true));
                            if (appViews1 != null) {
                                if (i == 0) appViews1.scrollToEnd(1);
                                else if (new Random().nextBoolean())
                                    appViews1.scrollToBeginning(1);
                                else appViews1.scrollToEnd(1);
                            }
                        } catch (UiObjectNotFoundException e) {
                            mDevice.swipe(startX, startY, endX, endX, 10);
                        }
                        sleep(timeOut * 1000);
                    }
                    sleep();
                    Log.d(TAG, "Thành công!");
                    oppenMainActivitySuccess();
                    return;
                }
                break;

        }

        checkView();
        sleep(1000);
        autoView();
    }

    public void checkClick() {
        UiObject2 btnAccep = mDevice.findObject(By.res("com.android.chrome:id/terms_accept"));
        if (btnAccep != null) {
            btnAccep.click();
            Log.d("SonLv", "Click Accept");
            sleep(1000);
        }

        btnAccep = mDevice.findObject(By.res("com.android.chrome:id/next_button"));
        if (btnAccep != null) {
            btnAccep.click();
            Log.d("SonLv", "Click Next");
            sleep(1000);
        }

        btnAccep = mDevice.findObject(By.res("com.android.chrome:id/negative_button"));
        if (btnAccep != null) {
            btnAccep.click();
            Log.d("SonLv", "Click No Thank");
        }

        UiObject2 btnMore = mDevice.findObject(By.text("More results"));
        if (btnMore == null) btnMore = mDevice.findObject(By.text("Kết quả khác"));
        if (btnMore != null) {
            btnMore.click();
            Log.d("SonLv", "Click Load More");
            page++;
        }

    }


    public void getData() {
        if (isFinish || SettingSupport.isStop(getApplicationContext())) {
            Log.d(TAG, "Kết thúc quá trình auto!");
            return;
        }
        countLoadData++;
        Log.d("SonLv", "Bắt đầu lấy data");
        TaskNet taskNet = new TaskNet(new BaseModel() {
            @Override
            public void onSuccess(int taskType, Object data, String msg) {
                super.onSuccess(taskType, data, msg);
                isLoadedData = true;
                objectScrip = (BaseObject) data;
                Log.d("SonLv", "Lấy data thành công: " + objectScrip.toJson());
            }

            @Override
            public void onFail(int taskType, String msg) {
                super.onFail(taskType, msg);
                Log.d("SonLv", "Lấy data thất bại " + msg);
                if (countLoadData < 5) {
                    getData();
                    return;
                }
                isLoadedData = true;
                oppenMainActivity("Không lấy được data");

            }
        }, TaskType.TASK_TASK, getApplicationContext());
        taskNet.exe();
    }


    public void checkView() {
        Log.d("checkView", "processView: " + step);
        try {
            List<UiObject2> listView = mDevice.findObjects(By.enabled(true));
            Log.d("checkView", "----------------------View-------------------------");
            Log.d("checkView", "list: " + listView.size());
            for (int i = 0; i < listView.size(); i++) {
                UiObject2 view = listView.get(i);
                Log.d("checkView", i + " getText: " + view.getClassName());
                Log.d("checkView", i + " getResourceName: " + view.getResourceName());
                Log.d("checkView", i + " getText: " + view.getText());
                Log.d("checkView", i + " getContentDescription: " + view.getContentDescription());
                Log.d("checkView", "-----------------------------------------------\n");
            }

        } catch (Exception e) {
            Log.d("checkView", "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sleep() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {

        }
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {

        }
    }

    public void offMobileData() {
        Log.d("SonLv", "OFFMobileData");
        try {
            OutputStream out = new ProcessBuilder(new String[0]).redirectErrorStream(true).command(new String[]{"su"}).start().getOutputStream();
            out.write("settings put global mobile_data 0\n".getBytes("UTF-8"));
            out.flush();
            out.close();
            Log.d("SonLv", "Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMobileData() {
        Log.d("SonLv", "ONMobileData");
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

    public void resetMobileData() {
        String result = Settings.Global.getString(getApplicationContext().getContentResolver(), "mobile_data");
        if (result.equals("0")) {
            onMobileData();
            sleep(3000);
            return;
        }

        offMobileData();
        sleep(1000);
        onMobileData();
        sleep(3000);
    }

    public void clearDataChrome() {
        try {
            Log.d("SonLv", "Bắt đầu clear data Chrome!");
            Process p = new ProcessBuilder(new String[0]).redirectErrorStream(true).command(new String[]{"su"}).start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            OutputStream out = p.getOutputStream();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("pm clear com.android.chrome\n");
            out.write(stringBuilder.toString().getBytes("UTF-8"));
            out.flush();
            out.write("exit\n".getBytes("UTF-8"));
            out.flush();
            out.close();
            p.waitFor();

            String line;
            while (true) {
                line = r.readLine();
                if (line == null) break;
                Log.d("SonLv", "line: " + line);
            }
            Log.d("SonLv", "Clear data Chrome thành công!");
        } catch (Exception e) {
            Log.d("SonLv", "Exception: " + e.getMessage());
            Log.d("SonLv", "Có lỗi khi clear data Chrome!");
        }
    }


    public void oppenMainActivity(String sms) {
        Log.d("SonLv", "sms: " + sms);
        isFinish = true;

        mDevice.pressHome();
        // Launch the blueprint app
        Context context = getApplicationContext();
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("sms", sms);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public void oppenMainActivitySuccess() {
        isFinish = true;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -3);
        SettingSupport.setEndTimeProcess(getApplicationContext(), calendar.getTimeInMillis());
        mDevice.pressHome();
        // Launch the blueprint app
        Context context = getApplicationContext();
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("sms", "Quá trình auto report kết thúc");
        intent.putExtra("success", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }


}
