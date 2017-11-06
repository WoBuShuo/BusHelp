package com.hkbushelp.apps;

import android.view.View;
import android.widget.Button;

import util.VersionUtil;

/**
 * Created by Hello on 2017/9/21.
 */

public class AboutActivity extends BaseActivity {
    @Override
    protected int setLayoutResID() {
        return R.layout.acitivity_about;
    }

    @Override
    public void initView() {
        setToolbar("關於我們",R.id.about_toolbar);
        Button button= (Button) findViewById(R.id.about_version);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查更新
                VersionUtil versionUtil=new VersionUtil();
                versionUtil.requestData(AboutActivity.this);
            }
        });
    }


}
