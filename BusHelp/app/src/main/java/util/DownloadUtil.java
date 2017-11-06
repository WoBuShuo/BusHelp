package util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;

import java.io.File;

import com.hkbushelp.apps.MyApplication;

/**
 * Created by Hello on 2017/9/11.
 */

public class DownloadUtil {
    private File mFile;
    private Context mContext;

    public void startDownload(Context context,String url,String name){

        if (!hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "很抱歉您没有给权限o", Toast.LENGTH_SHORT).show();
            return;
        }
        mContext=context;
        Aria.download(context)
                .load(url)     //读取下载地址
                .setDownloadPath(MyApplication.apkdownload_path+name+".apk")//设置文件保存的完整路径
                .start();//启动下载
        mFile=new File(MyApplication.apkdownload_path+name+".apk");
        Aria.download(context).addSchedulerListener(new MySchedulerListener());

    }

    private class MySchedulerListener extends Aria.DownloadSchedulerListener {

        @Override public void onTaskStart(DownloadTask task) {

        }

        @Override public void onTaskStop(DownloadTask task) {

        }

        @Override public void onTaskCancel(DownloadTask task) {

        }

        @Override public void onTaskFail(DownloadTask task) {

        }

        @Override public void onTaskComplete(DownloadTask task) {
            installAPK();

        }

        @Override public void onTaskRunning(DownloadTask task) {

        }
    }
    /**
     * 安装apk文件
     */
    public void installAPK() {
        if(mFile!=null){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);//动作
            intent.addCategory(Intent.CATEGORY_DEFAULT);//类型
            intent.setDataAndType(Uri.fromFile(mFile), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /**
     * 权限检查
     */
    public boolean hasPermission(String... permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
