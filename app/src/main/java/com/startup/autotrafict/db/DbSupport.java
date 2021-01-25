package com.startup.autotrafict.db;

import android.content.Context;
import android.util.Log;

import com.startup.autotrafict.Net.UrlObj;
import com.telpoo.frame.database.DbCacheUrl;
import com.telpoo.frame.object.BaseObject;

import java.util.ArrayList;

public class DbSupport {

    public static final void init(Context context) {
        MyDb.init(DbConfig.tables, DbConfig.keys, context, DbConfig.dbName, DbConfig.dbVersion);
        DbCacheUrl.initDb(context);
    }

    public static ArrayList<BaseObject> getUrls() {
        return MyDb.getAllOfTable(DbConfig.URL);
    }

    public static void saveUrl(String url) {
        ArrayList<BaseObject> arrayList = new ArrayList<>();
        BaseObject object = new BaseObject();
        object.set(UrlObj.url, url);
        arrayList.add(object);
        MyDb.addToTable(arrayList, DbConfig.URL);
    }


    public static void deleteUrl(String url) {
        Boolean save = MyDb.deleteRowInTable(DbConfig.URL, UrlObj.url, url);
        Log.d("DbSupport", save + " delete: " + url);
    }


}