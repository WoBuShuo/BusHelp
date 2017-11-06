package com.hkbushelp.apps;

import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.HttpUtil;

/**
 * 公交车经过详情的activity
 */

public class BusDetailsActivity extends BaseActivity {

    private BusDetailsAdapter mAdapter;
    private List<String> mBusContent;
    private BusTextAdapter mBusContentAdapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_bus_details;
    }

    private BusLineSearch mBusLineSearch;
    private static final String TAG = "bus_activity";
    private String city = "香港";

    @Override
    public void initView() {
        showDialog();
        String busNum = getIntent().getStringExtra("busNum");
        String busUrl = getIntent().getStringExtra("busUrl");
        View bottomSheet = findViewById(R.id.bus_details_bottom_sheet);
        if (busNum != null) {
            setToolbar(busNum, R.id.bus_details_toolbar);
        }
        if (busUrl == null) {
            // 第一步，创建POI检索实例
            PoiSearch poiSearch = PoiSearch.newInstance();
            // 第二步，设置POI检索监听者；
            poiSearch.setOnGetPoiSearchResultListener(poiListener);
            //第三步，设置城市和要查找的公交
            poiSearch.searchInCity((new PoiCitySearchOption()).city(city).keyword(busNum));
            // 第一步，创建Busline检索实例
            mBusLineSearch = BusLineSearch.newInstance();
            mBusLineSearch.setOnGetBusLineSearchResultListener(busLineListener);
            bottomSheet.setVisibility(View.GONE);
            LinearLayout linearLayout= (LinearLayout) findViewById(R.id.bus_details_linear_layout);
            CoordinatorLayout.LayoutParams lp=new CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.setLayoutParams(lp);
        } else {
            HttpUtil.getInstance().request(busUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    splitResult(response.body().string());
                }
            });
            mBusContent= new ArrayList<>();
            RecyclerView textRecycler= (RecyclerView) findViewById(R.id.bus_details_text_recycler);
            textRecycler.setLayoutManager(new LinearLayoutManager(this));
            mBusContentAdapter = new BusTextAdapter();
            textRecycler.setAdapter(mBusContentAdapter);

            final BottomSheetBehavior behavior=BottomSheetBehavior.from(bottomSheet);
            findViewById(R.id.bus_details_title).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
            });
        }


        mAdapter = new BusDetailsAdapter();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.bus_details_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

    }

    private void splitResult(String result) {
        Document document = Jsoup.parseBodyFragment(result);
        Element content = document.select("div.bus_label").first();
        List<TextNode> nodeList = content.select("p").first().textNodes();
        for (int i = 0; i <nodeList.size() ; i++) {
            String text=nodeList.get(i).text();
            int index=text.lastIndexOf(":");
            boolean is=text.length()-1==index;
            if (is){
                mBusContent.add(" ");
            }
            mBusContent.add(text);
            if (i==nodeList.size()-1){
                mBusContent.add(" ");
            }
        }


        Elements busElements = document.select("div.bus_site_layer");
        if (busElements.size() > 2 && busElements.size() <= 4) {

            setData(busElements,0);
            setData(busElements,1);
        } else if (busElements.size() <= 2) {
            setData(busElements,0);
        }else if (busElements.size()>4){
            setData(busElements,0);
            setData(busElements,1);
            setData(busElements,2);
        }

        handler.sendEmptyMessage(87);
    }

    private void setData( Elements busElements,int i){
        Elements a = busElements.get(i).select("a");
        for (Element element : a) {
            String replace1 = element.text().replace("︵", "(");
            String replace2 = replace1.replace("︶", ")");
            String replace3 = replace2.replace("︹", "[");
            String replace4 = replace3.replace("︺", "]");
            mDatas.add(replace4);
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAdapter.notifyDataSetChanged();
            mBusContentAdapter.notifyDataSetChanged();
            dissDialog();
        }
    };

    class BusDetailsAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public BusDetailsAdapter() {
            super(R.layout.item_bus_details, mDatas);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_bus_details_name, item);
//            helper.set
            int position = helper.getAdapterPosition();
            if (position == 0) {
//                helper.setVisible(R.id.item_bus_details_up,false);
                helper.setAlpha(R.id.item_bus_details_up, 0);
            } else {
                helper.setAlpha(R.id.item_bus_details_up, 1);
            }

            if (position == mDatas.size() - 1) {
                helper.setAlpha(R.id.item_bus_details_down, 0);
            } else {
                helper.setAlpha(R.id.item_bus_details_down, 1);
            }
            helper.setText(R.id.item_bus_details_position, (helper.getAdapterPosition() + 1) + "");
        }
    }

    private String busLineId;
    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult result) {
            Log.e(TAG, "onGetPoiResult: " + result);
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                return;
            }
            //遍历所有POI，找到类型为公交线路的POI
            for (PoiInfo poi : result.getAllPoi()) {
                if (poi.type == PoiInfo.POITYPE.BUS_LINE || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                    //说明该条POI为公交信息，获取该条POI的UID
                    busLineId = poi.uid;
                    Log.e(TAG, "onGetPoiResult: " + busLineId);
                    mBusLineSearch.searchBusLine((new BusLineSearchOption()
                            .city(city)
                            .uid(busLineId)));
                    break;
                }
            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    private List<String> mDatas = new ArrayList<>();
    OnGetBusLineSearchResultListener busLineListener = new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult result) {
            String title;
            List<BusLineResult.BusStation> stations = result.getStations();
            dissDialog();
            for (BusLineResult.BusStation busStation : stations) {
                title = busStation.getTitle();
                mDatas.add(title);
            }
            mAdapter.notifyDataSetChanged();
        }
    };


    class BusTextAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public BusTextAdapter() {
            super(R.layout.item_single, mBusContent);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_single_text,item);
        }
    }
}
