package com.hkbushelp.apps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.Global;
import util.HttpUtil;

/**
 * 换乘方案activity
 */

public class RoutePlanActivity extends BaseActivity {
    private RoutePlanAdapter mAdapter;
    private List<List<String>> mAllPlanList = new ArrayList<>();
    public static final String ROUTE_PLAN_ONE = "one";//一次车，不用换乘
    public static final String ROUTE_PLAN_TWO = "two";//二次车，需要换乘一次
    public static final String ROUTE_PLAN_SHREE = "three";
    private String ROUTE_PLAN_NUM = ROUTE_PLAN_ONE;
    private List<Integer> mWalkList = new ArrayList<>();
    private Intent mIntent;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_route_plan;
    }

    @Override
    public void initView() {
        showDialog();
        mIntent = getIntent();
        String startLongitude = mIntent.getStringExtra("start_longitude");
        String startLatitude = mIntent.getStringExtra("start_latitude");
        String endLongitude = mIntent.getStringExtra("end_longitude");
        String endLatitude = mIntent.getStringExtra("end_latitude");
        final String startName=mIntent.getStringExtra("start_name");
        final String endName=mIntent.getStringExtra("end_name");
        setToolbar("換乘方案", R.id.route_plan_toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.route_plan_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RoutePlanAdapter();
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mIntent = new Intent(RoutePlanActivity.this, PlanActivity.class);
                List<String> planList = mAllPlanList.get(position);
//                if (planList.get(planList.size()-1).equals(ROUTE_PLAN_ONE)){
//                    mIntent.putExtra("ROUTE_PLAN_NUM",ROUTE_PLAN_ONE);
//
//                }else if (planList.get(planList.size()-1).equals(ROUTE_PLAN_TWO)){
//
//                }else if (planList.get(planList.size() - 1).equals(ROUTE_PLAN_SHREE)){
//
//                }
                mIntent.putExtra("plan_list", (Serializable) planList);
                mIntent.putExtra("start_name",startName);
                mIntent.putExtra("end_name",endName);
                startActivity(mIntent);
            }
        });


        HttpUtil.getInstance().request(Global.getRoutePlanUrl(startLatitude, startLongitude, endLatitude, endLongitude), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                splitResult(response.body().string());
            }
        });
    }

    private void splitResult(String result) {
        if(result.equals("[No Result]")){
            mHandler.sendEmptyMessage(NO_FIND);
        }


        String[] split = result.split("\\<br\\>");
        int walk1, walk2, walk3 = 0, walk4 = 0;//步行的距离

        for (int i = 0; i < split.length - 1; i++) {
            String[] planInfo = split[i].split("\\|\\|");
            //说明不是直达的，需要换乘,是两条路线
            if (planInfo.length > 20 && planInfo.length < 38) {
                ROUTE_PLAN_NUM = ROUTE_PLAN_TWO;
                walk1 = Integer.parseInt(planInfo[2]);
                walk2 = Integer.parseInt(planInfo[3]);
                walk3 = Integer.parseInt(planInfo[4]);
                String[] busNum = planInfo[7].split("-");
                planInfo[0] = busNum[0];//把车路线号放在第0
                String[] busNum2 = planInfo[22].split("-");
                planInfo[1] = busNum2[0];//把车路线号放在第1
            } else if (planInfo.length <= 20) {//直接到达，一次乘车
                String[] busNum = planInfo[6].split("-");
                planInfo[0] = busNum[0];
                ROUTE_PLAN_NUM = ROUTE_PLAN_ONE;
                walk1 = Integer.parseInt(planInfo[2]);
                walk2 = Integer.parseInt(planInfo[3]);
            } else {
                ROUTE_PLAN_NUM = ROUTE_PLAN_SHREE;
                walk1 = Integer.parseInt(planInfo[2]);
                walk2 = Integer.parseInt(planInfo[3]);
                walk3 = Integer.parseInt(planInfo[4]);
                walk4 = Integer.parseInt(planInfo[5]);
                String[] busNum = planInfo[8].split("-");
                planInfo[0] = busNum[0];//把车路线号放在第0
                String[] busNum2 = planInfo[23].split("-");
                planInfo[1] = busNum2[0];
                String[] busNum3 = planInfo[38].split("-");
                planInfo[planInfo.length - 1] = busNum3[0];
            }
            mWalkList.add(walk1 + walk2 + walk3 + walk4);
            List<String> planList = Arrays.asList(planInfo);
            List<String> routePlanList = new ArrayList<>();
            routePlanList.addAll(planList);
            routePlanList.add(ROUTE_PLAN_NUM);
            mAllPlanList.add(routePlanList);

        }

        mHandler.sendEmptyMessage(FIND);
    }

    private final int FIND=25;
    private final int NO_FIND=26;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==FIND){
                mAdapter.notifyDataSetChanged();
            }else if(msg.what==NO_FIND){
                new AlertDialog.Builder(RoutePlanActivity.this)
                        .setTitle("妳所選擇的起點和終點在指定的時間沒有路線相連")
                        .setNegativeButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            }
            dissDialog();
        }
    };

    private class RoutePlanAdapter extends BaseQuickAdapter<List<String>, BaseViewHolder> {

        private RoutePlanAdapter() {
            super(R.layout.item_route_plan, mAllPlanList);
        }

        @Override
        protected void convert(BaseViewHolder helper, List<String> item) {
            if (item.get(item.size() - 1).equals(ROUTE_PLAN_ONE)) {
                helper.setVisible(R.id.item_route_plan_image1, false);
                helper.setVisible(R.id.item_route_plan_line2, false);
                helper.setVisible(R.id.item_route_plan_money2, false);
                helper.setVisible(R.id.item_route_plan_money3,false);
                helper.setVisible(R.id.item_route_plan_line3,false);
                helper.setVisible(R.id.item_route_plan_image2,false);
                helper.setText(R.id.item_route_plan_line1, item.get(0));
                helper.setText(R.id.item_route_plan_money, "￥" + item.get(10));
            } else if (item.get(item.size() - 1).equals(ROUTE_PLAN_TWO)) {
                helper.setVisible(R.id.item_route_plan_image1, true);
                helper.setVisible(R.id.item_route_plan_money2, true);
                helper.setVisible(R.id.item_route_plan_line2, true);
                helper.setVisible(R.id.item_route_plan_money3,false);
                helper.setVisible(R.id.item_route_plan_line3,false);
                helper.setVisible(R.id.item_route_plan_image2,false);
                helper.setText(R.id.item_route_plan_line1, item.get(0));
                helper.setText(R.id.item_route_plan_line2, item.get(1));
                helper.setText(R.id.item_route_plan_money2, "￥" + item.get(26));
                helper.setText(R.id.item_route_plan_money, "￥" + item.get(11));
            } else if (item.get(item.size() - 1).equals(ROUTE_PLAN_SHREE)) {
                helper.setVisible(R.id.item_route_plan_image1, true);
                helper.setVisible(R.id.item_route_plan_money2, true);
                helper.setVisible(R.id.item_route_plan_line2, true);
                helper.setVisible(R.id.item_route_plan_money3,true);
                helper.setVisible(R.id.item_route_plan_line3,true);
                helper.setVisible(R.id.item_route_plan_image2,true);
                helper.setText(R.id.item_route_plan_line1, item.get(0));
                helper.setText(R.id.item_route_plan_line2, item.get(1));
                helper.setText(R.id.item_route_plan_line3,item.get(item.size() - 2));
                helper.setText(R.id.item_route_plan_money2, "￥" + item.get(27));
                helper.setText(R.id.item_route_plan_money, "￥" + item.get(12));
                helper.setText(R.id.item_route_plan_money3, "￥" + item.get(42));
            }
            helper.setText(R.id.item_route_plan_walk_distance, "步行約" + mWalkList.get(helper.getAdapterPosition()) + "米");
        }
    }

}
