package com.hkbushelp.apps;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Arrays;
import java.util.List;


/**
 * 显示附近公交站点的经过的公交列表
 */

public class StationDetailsActivity extends BaseActivity {
    private List<String> mBusNumList;
    String mDistrict;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_station_details;
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        String busNun = intent.getStringExtra("busNuns");
        String stationName = intent.getStringExtra("stationName");
        mDistrict = intent.getStringExtra("district");
        String[] split = busNun.split(";");
        mBusNumList = Arrays.asList(split);

        setToolbar(stationName, R.id.station_details_toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.station_details_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        StationDetailsAdapter adapter = new StationDetailsAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent1 = new Intent(StationDetailsActivity.this, BusDetailsActivity.class);
                intent1.putExtra("busNum", mBusNumList.get(position));
                startActivity(intent1);
            }
        });
    }


    class StationDetailsAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public StationDetailsAdapter() {
            super(R.layout.item_station_details, mBusNumList);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_station_details_bus, item);
        }
    }
}
