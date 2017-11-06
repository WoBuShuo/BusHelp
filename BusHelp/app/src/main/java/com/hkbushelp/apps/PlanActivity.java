package com.hkbushelp.apps;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.Global;
import util.HttpUtil;

/**
 * 换乘详情activity、
 */

public class PlanActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView mRecyclerViewOne;
    private RecyclerView mRecyclerViewTwo;
    private RecyclerView mRecyclerViewThree;
    private TextView mWalkTextTwo;
    private TextView mWalkTextOne;
    private TextView mBusDirectionThree;
    private TextView mBusDirectionTwo;
    private TextView mBusDirectionOne;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_plan;
    }

    private List<String> mPlanList;
    private List<List<String>> mAllStationName = new ArrayList<>();
    private int mBus1Index, mBus3Index, mBus2Index;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dissDialog();
            setData();
        }
    };

    private void setData() {

        PlanAdapter adapterOne = new PlanAdapter(mBusByStation1);
        PlanAdapter adapterTwo = new PlanAdapter(mBusByStation2);
        PlanAdapter adapterThree = new PlanAdapter(mBusByStation3);

        mRecyclerViewOne.setAdapter(adapterOne);
        mRecyclerViewTwo.setAdapter(adapterTwo);
        mRecyclerViewThree.setAdapter(adapterThree);
        if (Integer.parseInt(mPlanList.get(3)) < 15) {
            mWalkTextOne.setText("同站換乘");
        } else {
            mWalkTextOne.setText("步行" + mPlanList.get(3) + "m");
        }
        switch (mPlanList.get(mPlanList.size() - 1)) {
            case RoutePlanActivity.ROUTE_PLAN_ONE:
                String oneBusType = mPlanList.get(5).equals("CTB") ? "城巴" : "新巴";
                mBusDirectionOne.setText(mPlanList.get(0) + "(" + oneBusType + "/往" + mAllStationName.get(0).get(8) + "方向)");
                break;
            case RoutePlanActivity.ROUTE_PLAN_TWO:
//                String busType2 = mPlanList.get(5).equals("CTB") ? "城巴" : "新巴";
                String twoBusType1 = mPlanList.get(6).equals("CTB") ? "城巴" : "新巴";
                String twoBusType2 = mPlanList.get(21).equals("CTB") ? "城巴" : "新巴";
                mBusDirectionOne.setText(mPlanList.get(0) + "(" + twoBusType1 + "/往" + mAllStationName.get(0).get(8) + "方向)");
                mBusDirectionTwo.setText(mPlanList.get(1) + "(" + twoBusType2 + "/往" + mAllStationName.get(mBus2Index).get(8) + "方向)");
                break;
            case RoutePlanActivity.ROUTE_PLAN_SHREE:
                String threeBusType1 = mPlanList.get(7).equals("CTB") ? "城巴" : "新巴";
                String threeBusType2 = mPlanList.get(22).equals("CTB") ? "城巴" : "新巴";
                String threeBusType3 = mPlanList.get(37).equals("CTB") ? "城巴" : "新巴";
                if (Integer.parseInt(mPlanList.get(4)) < 15) {
                    mWalkTextTwo.setText("同站換乘");
                } else {
                    mWalkTextTwo.setText("步行" + mPlanList.get(4) + "m");
                }
                mBusDirectionOne.setText(mPlanList.get(0) + "(" + threeBusType1 + "/往" + mAllStationName.get(0).get(8) + "方向)");
                mBusDirectionTwo.setText(mPlanList.get(1) + "(" + threeBusType2 + "/往" + mAllStationName.get(mBus2Index).get(8) + "方向)");
                mBusDirectionThree.setText(mPlanList.get(mPlanList.size() - 2) + "(" +
                        threeBusType3 + "/往" + mAllStationName.get(mBus3Index).get(8) + "方向)");

                break;
        }
    }


    @Override
    public void initView() {
        showDialog();
        setToolbar("換乘詳情", R.id.plan_toolbar);
        Intent intent = getIntent();
        mPlanList = (List<String>) intent.getSerializableExtra("plan_list");
        String startName = intent.getStringExtra("start_name");
        String endName = intent.getStringExtra("end_name");

        TextView startAddress = (TextView) findViewById(R.id.plan_start_address);
        TextView startWalk = (TextView) findViewById(R.id.plan_start_walk);
        TextView endAddress = (TextView) findViewById(R.id.plan_end_address);
        TextView endWalk = (TextView) findViewById(R.id.plan_end_walk);
        mBusDirectionOne = (TextView) findViewById(R.id.plan_bus_direction1);
        mBusDirectionTwo = (TextView) findViewById(R.id.plan_bus_direction2);
        mBusDirectionThree = (TextView) findViewById(R.id.plan_bus_direction3);
        mWalkTextOne = (TextView) findViewById(R.id.plan_walk1);
        mWalkTextTwo = (TextView) findViewById(R.id.plan_walk2);
        mRecyclerViewOne = (RecyclerView) findViewById(R.id.plan_station_name_recycler1);
        mRecyclerViewTwo = (RecyclerView) findViewById(R.id.plan_station_name_recycler2);
        mRecyclerViewThree = (RecyclerView) findViewById(R.id.plan_station_name_recycler3);
        LinearLayout planLayoutOne = (LinearLayout) findViewById(R.id.plan_bus1_layout);
        LinearLayout planLayoutTwo = (LinearLayout) findViewById(R.id.plan_bus2_layout);
        LinearLayout planLayoutThree = (LinearLayout) findViewById(R.id.plan_bus3_layout);
        planLayoutOne.setOnClickListener(this);
        planLayoutTwo.setOnClickListener(this);
        planLayoutThree.setOnClickListener(this);
        LinearLayout walkLayoutOne = (LinearLayout) findViewById(R.id.plan_walk1_layout);
        LinearLayout walkLayoutTwo = (LinearLayout) findViewById(R.id.plan_walk2_layout);

        mRecyclerViewOne.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewTwo.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewThree.setLayoutManager(new LinearLayoutManager(this));

        startAddress.setText(startName);
        endAddress.setText(endName);
        startWalk.setText("步行" + mPlanList.get(2) + "m");
        String info = "";
        if (mPlanList.get(mPlanList.size() - 1).equals(RoutePlanActivity.ROUTE_PLAN_ONE)) {
            info = "1%7C%2A%7C" + mPlanList.get(5) + "%7C%7C" + mPlanList.get(6) + "%7C%7C" + mPlanList.get(7) +
                    "%7C%7C" + mPlanList.get(8);
            walkLayoutOne.setVisibility(View.GONE);
            walkLayoutTwo.setVisibility(View.GONE);
            planLayoutTwo.setVisibility(View.GONE);
            planLayoutThree.setVisibility(View.GONE);
            endWalk.setText("步行" + mPlanList.get(3) + "m");
        } else if (mPlanList.get(mPlanList.size() - 1).equals(RoutePlanActivity.ROUTE_PLAN_TWO)) {
            info = "2%7C%2A%7C" + mPlanList.get(6) + "%7C%7C" + mPlanList.get(7) + "%7C%7C" + mPlanList.get(8) +
                    "%7C%7C" + mPlanList.get(9)
                    + "%7C%2A%7C" + mPlanList.get(21) + "%7C%7C" + mPlanList.get(22) + "%7C%7C" + mPlanList.get(23) +
                    "%7C%7C" + mPlanList.get(24);
            walkLayoutTwo.setVisibility(View.GONE);
            planLayoutThree.setVisibility(View.GONE);
            endWalk.setText("步行" + mPlanList.get(4) + "m");
        } else if (mPlanList.get(mPlanList.size() - 1).equals(RoutePlanActivity.ROUTE_PLAN_SHREE)) {
            info = "3%7C%2A%7C" + mPlanList.get(7) + "%7C%7C" + mPlanList.get(8) + "%7C%7C" + mPlanList.get(9) +
                    "%7C%7C" + mPlanList.get(10)
                    + "%7C%2A%7C" + mPlanList.get(22) + "%7C%7C" + mPlanList.get(23) + "%7C%7C" + mPlanList.get(24) +
                    "%7C%7C" + mPlanList.get(25)
                    + "%7C%2A%7C" + mPlanList.get(37) + "%7C%7C" + mPlanList.get(38) + "%7C%7C" + mPlanList.get(39) +
                    "%7C%7C" + mPlanList.get(40);
            endWalk.setText("步行" + mPlanList.get(5) + "m");
        }


        HttpUtil.getInstance().request(Global.getPlanDetails(info), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                splitResult(response.body().string());
            }


        });

    }


    //每次巴士经过的站点list
    private List<String> mBusByStation1 = new ArrayList<>();
    private List<String> mBusByStation2 = new ArrayList<>();
    private List<String> mBusByStation3 = new ArrayList<>();
    //记录recycler的伸缩
    private boolean mIsCall1=true,mIsCall2=true,mIsCall3=true;

    private void splitResult(String result) {
        String[] split = result.split("\\<br\\>");
        for (int i = 0; i < split.length - 1; i++) {
            String[] split1 = split[i].split("\\|\\|");
            List<String> stationName = Arrays.asList(split1);
            mAllStationName.add(stationName);
            if (split1[0].equals("X1")) {
                mBus1Index = i;
                mBusByStation1.add(split1[7]);
            } else if (split1[0].equals("X2")) {
                mBus2Index = i;
                mBusByStation2.add(split1[7]);
            } else if (split1[0].equals("X3")) {
                mBus3Index = i;
                mBusByStation3.add(split1[7]);
            }
        }
        mHandler.sendEmptyMessage(63);
    }

    @Override
    public void onClick(View view) {
        LinearLayout.LayoutParams lpLaunch = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams lpCall = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 2);
        switch (view.getId()) {
            case R.id.plan_bus1_layout:
                if (mIsCall1){
                    mRecyclerViewOne.setLayoutParams(lpLaunch);
                }else{
                    mRecyclerViewOne.setLayoutParams(lpCall);
                }
                mIsCall1=!mIsCall1;
                break;

            case R.id.plan_bus2_layout:
                if (mIsCall2){
                    mRecyclerViewTwo.setLayoutParams(lpLaunch);
                }else{
                    mRecyclerViewTwo.setLayoutParams(lpCall);
                }
                mIsCall2=!mIsCall2;
                break;

            case R.id.plan_bus3_layout:
                if (mIsCall3){
                    mRecyclerViewThree.setLayoutParams(lpLaunch);
                }else{
                    mRecyclerViewThree.setLayoutParams(lpCall);
                }
                mIsCall3=!mIsCall3;
                break;
        }
    }


    class PlanAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public PlanAdapter(@Nullable List<String> data) {
            super(R.layout.item_plan, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_plan_text, item);
        }
    }

}
