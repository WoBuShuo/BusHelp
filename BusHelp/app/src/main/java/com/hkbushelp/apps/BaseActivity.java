package com.hkbushelp.apps;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import util.Invoke;
import view.LoadDialog;


/**
 * Created by Administrator on 2017/5/19.
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutResID());
        initView();
        initData();
    }

    protected abstract int setLayoutResID();

    /**
     * 初始化UI
     */
    public abstract void initView(
    );

    public void initData() {

    }

    private Toast toast = null;

    protected void showToast(String msg, int length) {
        if (toast == null) {
            toast = Toast.makeText(this, msg, length);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }


    public void setToolbar(String title, int id) {
        Toolbar toolbar = (Toolbar) findViewById(id);
        // 主标题,默认为app_label的名字
        toolbar.setTitle(title);
        toolbar.setTitleTextColor(Color.WHITE);
        //侧边栏的按钮
        toolbar.setNavigationIcon(R.mipmap.back_arrow_ic);
        //取代原本的actionbar
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private LoadDialog mLoadDialog;

    public void showDialog() {
        mLoadDialog = new LoadDialog(this, true, "100倍加速中");
        mLoadDialog.show();
    }

    public void dissDialog() {
        mLoadDialog.dismiss();
    }


    /**
     * 权限检查
     */
    public boolean hasPermission(String... permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求权限
     */
    public void requestPermission(int code, String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, code);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults[2] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "沒有權限無法定位哦", Toast.LENGTH_SHORT).show();
        }


        if (grantResults[4] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "没有权限有些功能无法使用哦", Toast.LENGTH_SHORT).show();
        } else if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
            new Invoke().Test(this);
        }

    }

    public void writePermission() {

    }

    /**
     * 设置状态栏透明
     *
     * @param on
     */
    @TargetApi(19)
    public void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
