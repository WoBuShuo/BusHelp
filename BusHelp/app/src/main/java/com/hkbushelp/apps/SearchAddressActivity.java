package com.hkbushelp.apps;

import android.content.Intent;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fragment.BusFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import util.Global;

/**
 * 搜索地址activity
 */

public class SearchAddressActivity extends BaseActivity {

    private String TAG = "tag";

    private SearchAddressAdapter mAddressAdapter;

    private android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {

            mAddressAdapter.notifyDataSetChanged();
        }
    };


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_search_address;
    }

    @Override
    public void initView() {
        String hintString=this.getIntent().getStringExtra(BusFragment.HINT_STRING);

        setToolbar("", R.id.search_address_toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_address_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAddressAdapter = new SearchAddressAdapter();
        recyclerView.setAdapter(mAddressAdapter);


        SearchView searchView = (SearchView) findViewById(R.id.search_address_search_view);
        //默认展开搜索框
        searchView.onActionViewExpanded();
        searchView.setQueryHint(hintString);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                request(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                request(newText);
                return true;
            }
        });

        mAddressAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent=new Intent();
                intent.putExtra("address_name",addressList.get(position).get(0));
                intent.putExtra("address_longitude",addressList.get(position).get(1));
                intent.putExtra("address_latitude",addressList.get(position).get(2));
                setResult(RESULT_OK,intent);
                finish();
            }
        });


    }

    private void request(String address) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(Global.getSearchStation(address)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Log.e(TAG, "onResponse: "+response.body().string() );
                splitResult(response.body().string());
            }
        });
    }

    private List<List<String>> addressList = new ArrayList<>();

    private void splitResult(String result) {
        String[] split = result.split("\\|\\*\\|");
        addressList.clear();
        //减去最后一个空白的数据
        for (int i = 0; i < split.length - 1; i++) {
            String[] addressInfo = split[i].split("\\|\\|");
            //美利道停车场大厦||22.280220000000||114.162070000000||Murray Road Carpark Building
            //纬度范围-90~90,longitude,经度范围-180~180,latitude
            //这个list存储的数据就这上面的四个
            List<String> list = Arrays.asList(addressInfo);
            addressList.add(list);
        }

        mHandler.sendEmptyMessage(100);
    }

    class SearchAddressAdapter extends BaseQuickAdapter<List<String>, BaseViewHolder> {

        public SearchAddressAdapter() {
            super(R.layout.item_search_address, SearchAddressActivity.this.addressList);
        }

        @Override
        protected void convert(BaseViewHolder helper, List<String> item) {
            helper.setText(R.id.item_search_address_name, item.get(0));
            if (item.size() > 3) {
                helper.setText(R.id.item_search_address_english_name, item.get(3));
            }else{
                helper.setText(R.id.item_search_address_english_name, "");
            }
        }
    }
}
