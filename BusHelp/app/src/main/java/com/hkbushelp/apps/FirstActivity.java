package com.hkbushelp.apps;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import fragment.BusFragment;
import fragment.FirstFragmentAdapter;
import fragment.SearchFragment;
import fragment.NearbyFragment;
import util.ADUtil;
import util.Global;
import util.Invoke;

/**
 * Created by Hello on 2017/8/23.
 */

public class FirstActivity extends BaseActivity {

    private ImageView mAdView;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_first;
    }

    @Override
    public void initView() {
        mAdView = (ImageView) findViewById(R.id.first_ad_image);


        ViewPager viewPager = (ViewPager) findViewById(R.id.first_pager);
        ImageView imageView= (ImageView) findViewById(R.id.activity_first_title_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,AboutActivity.class));
            }
        });
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new BusFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(new NearbyFragment());
        viewPager.setAdapter(new FirstFragmentAdapter(getFragmentManager(), fragmentList));
        viewPager.setOffscreenPageLimit(fragmentList.size());
        TabLayout tabLayout = (TabLayout) findViewById(R.id.first_tab);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);

        ADUtil adUtil=new ADUtil();
        adUtil.requestAdertisement(this,mAdView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE)) {
            new Invoke().Test(this);
        } else {
            requestPermission(Global.WRITE_READ_EXTERNAL_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE);
        }
    }

}
