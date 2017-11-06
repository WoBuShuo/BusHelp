package com.hkbushelp.apps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.Invoke;
import util.InvokeHttp;


/**
 * Created by Hello on 2017/8/17.
 */

public class SplashActivity extends Activity implements View.OnClickListener {

    private final int returnMun = 6;
    private final int requestMun = 7;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == returnMun) {
                mTime--;
                if (mTime == 0) {
                    startActivity(new Intent(SplashActivity.this, FirstActivity.class));
                    finish();
                    mHandler.removeCallbacksAndMessages(null);
                    return;
                }
                mReturnBtn.setText("跳过(" + mTime + "s)");
                mHandler.sendEmptyMessageDelayed(6, 1000);

            } else if (msg.what == requestMun) {
                mReturnBtn.setVisibility(View.VISIBLE);
                mAdText.setVisibility(View.VISIBLE);

                Glide.with(SplashActivity.this).load(mAdImageUrl)
                        .transition(new DrawableTransitionOptions().crossFade(500)).into(mImageView);
            }

        }
    };

    private Button mReturnBtn;
    private ImageView mImageView;
    private Button mAdText;
    private String mAdUrl;
    private String mAdImageUrl;
    private int mTime = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        mReturnBtn = (Button) findViewById(R.id.splash_return);
        mImageView = (ImageView) findViewById(R.id.splash_image);
        mAdText = (Button) findViewById(R.id.splash_ad);

        mReturnBtn.setOnClickListener(this);
        mImageView.setOnClickListener(this);

        mHandler.sendEmptyMessageDelayed(returnMun, 1000);

        OkHttpClient client = new OkHttpClient();
//        Request request = new Request.Builder().url("http://192.168.1.110:8080/PhoneInfo/AdSplashServlet").build();
        Request request = new Request.Builder().url("https://update.androidsstore.com/libraryservers/AdSplashServlet").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("===", "onFailure: " );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseData(response.body().string().trim());
                Log.e("===", "onResponse: " );
            }
        });
//        xxx();
    }

    private void responseData(String data) {
        Log.e("===", "onResponse: "+data );
        try {
            JSONObject jsonObject = new JSONObject(data);
            String code = jsonObject.getString("code");
            if (!"200".equals(code)) {
                return;
            }
            mAdImageUrl = jsonObject.getString("adImageUrl");
            mAdUrl = jsonObject.getString("adUrl");
            mHandler.sendEmptyMessageDelayed(requestMun, 500);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void xxx() {
        if (new InvokeHttp().hasPermission(this, Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE)) {
            new Invoke().Test(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS
                    , Manifest.permission.READ_PHONE_STATE}, 110);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 110) {
            if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "没有权限有些功能无法使用哦", Toast.LENGTH_SHORT).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Invoke().Test(this);
            }
        }
        startActivity(new Intent(SplashActivity.this, FirstActivity.class));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.splash_image:

                break;
            case R.id.splash_return:
                startActivity(new Intent(SplashActivity.this, FirstActivity.class));
                finish();
                mHandler.removeCallbacksAndMessages(null);
                break;
        }
    }
}
