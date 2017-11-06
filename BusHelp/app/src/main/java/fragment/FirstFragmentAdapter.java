package fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Hello on 2017/8/24.
 */

public class FirstFragmentAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragmentList;
    private String[] mTitle=new String[]{"路線","搜索","附近","路線"};

    public FirstFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragmentList = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList == null ? 0 : mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitle[position];
    }
}
