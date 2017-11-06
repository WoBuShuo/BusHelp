package com.hkbushelp.apps;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.baidu.mapapi.SDKInitializer;

import java.io.File;

/**
 * Created by Hello on 2017/9/11.
 */

public class MyApplication extends Application {
    public static String apkdownload_path="";
    public static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        apkdownload_path= Environment.getExternalStorageDirectory().getPath()+ File.separator+"BusHelp";
        File file=new File(apkdownload_path);
        if(!file.exists()){
            file.mkdirs();
        }
        apkdownload_path+=File.separator;
        mAppContext=getApplicationContext();

    }


}
