package fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import com.hkbushelp.apps.R;
import com.hkbushelp.apps.StationDetailsActivity;
import overlayutil.WalkingRouteOverlay;
import util.MyOrientationListener;

/**
 * Created by Hello on 2017/9/4.
 */

public class NearbyFragment extends Fragment implements View.OnClickListener, OnGetRoutePlanResultListener {
    private Activity mActivity;
    private LocationClient mLocationClient;
    private LatLng mLatLng;
    private String mDistrict;
    private RecyclerView mRecyclerView;
    private QuickAdapter mQuickAdapter;
    private List<StationInfo> mStationInfoList;
    private BitmapDescriptor mBitmapDescriptor;
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private MyOrientationListener mOrientationListener;
    private float currentX;

    //记录recyclerview点击的位置
    private int position;
    //存储各个站点经纬度的list
    private List<LatLng> mLatList = new ArrayList<>();


    private int leverSmall = 61;
    private int leverBig = 62;
    private int lever=leverSmall;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == leverSmall) {
                mBaiduMap.clear();
                lever=leverSmall;
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(mLatLng).zoom(18.5f).build()));//设置缩放级别
            } else if (msg.what == leverBig) {
                lever=leverBig;
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().target(mLatLng).zoom(19.0f).build()));//设置缩放级别
            }
        }
    };
    private BottomSheetDialog mDialog;
    private TextView mTitle;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, null);
        mActivity = getActivity();
        mStationInfoList = new ArrayList<>();
        mQuickAdapter = new QuickAdapter();
        mMapView = (MapView) view.findViewById(R.id.nearby_fragment_map_view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.nearby_fragment_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mRecyclerView.setAdapter(mQuickAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mActivity,
                DividerItemDecoration.VERTICAL));

        showBSDialog();
        mQuickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                NearbyFragment.this.position = position;
                mRecyclerView.setVisibility(View.GONE);
                mHandler.sendEmptyMessageDelayed(leverBig, 500);
                mDialog.show();
                mTitle.setText(mStationInfoList.get(position).getName());

                BitmapDescriptor busIcon = BitmapDescriptorFactory.fromResource(R.mipmap.nearby_station);
                OverlayOptions options = new MarkerOptions().position(mLatList.get(position)).icon(busIcon).anchor(0.5f, 1.0f)
                        .zIndex(8);

                mBaiduMap.addOverlay(options);
                TextOptions textOptions = new TextOptions();
                textOptions.bgColor(Color.parseColor("#ffffff"))
                        .fontSize(22).fontColor(Color.parseColor("#FF5A00")).align(TextOptions.ALIGN_CENTER_HORIZONTAL, TextOptions.ALIGN_TOP)
                        .text(mStationInfoList.get(position).getName()).position(mLatList.get(position));
                mBaiduMap.addOverlay(textOptions);
            }
        });
        initMap();

        return view;
    }


    private void showBSDialog() {
        mDialog = new BottomSheetDialog(mActivity, R.style.BottomSheetDialogStyle);
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.bus_fragment_sheet, null);
        mDialog.setContentView(contentView);

        mTitle = (TextView) contentView.findViewById(R.id.sheet_title);
        TextView walk = (TextView) contentView.findViewById(R.id.sheet_walk);
        TextView details = (TextView) contentView.findViewById(R.id.sheet_details);

        mTitle.setOnClickListener(this);
        walk.setOnClickListener(this);
        details.setOnClickListener(this);

        View parent = (View) contentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        contentView.measure(0, 0);
        behavior.setPeekHeight(contentView.getMeasuredHeight());
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        parent.setLayoutParams(params);
    }


    public void initMap() {
        mLocationClient = new LocationClient(getActivity());
        //监听我的位置
        MyLocationListener myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
//        可选，默认gcj02，设置返回的定位结果坐标系
        int span = 7000;
        option.setScanSpan(span);
//        可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        mLocationClient.setLocOption(option);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMaxAndMinZoomLevel(21.0f, 8.0f);
        //开启我的位置图层
        mBaiduMap.setMyLocationEnabled(true);
        //更改我的位置图标
//        mBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);
//        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
//                true, mBitmapDescriptor);
//        mBaiduMap.setMyLocationConfiguration(config);
//        mMapView
        //请求viewpager不要拦截地图的滑动事件
        mMapView.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });


        mMapView.showScaleControl(false);
        mMapView.showZoomControls(false);
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (lever==leverSmall){
                    return;
                }
                mDialog.show();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        mOrientationListener = new MyOrientationListener(mActivity);
        mOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChange(float x) {
                currentX = x;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.start();
        mOrientationListener.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mOrientationListener.stop();
        mLocationClient.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
//        mLocationClient.stop();
    }

    //计算当前位置到公交站的距离
    public double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double pk = 180 / 3.14169;
        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return 6371000 * tt;
    }

    //搜索附近公交站台
    private void searchStation() {
        PoiSearch poiSearch = PoiSearch.newInstance();
        poiSearch.setOnGetPoiSearchResultListener(mListener);
        poiSearch.searchNearby(new PoiNearbySearchOption().keyword("公交站").location(mLatLng).radius(1000));
        poiSearch.destroy();
    }

    //搜索公交结果
    OnGetPoiSearchResultListener mListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            if (poiResult.getAllPoi() == null) {
                Log.e(TAG, "onGetPoiResult: ======null");
                return;
            }
            mStationInfoList.clear();
            for (PoiInfo poiInfo : poiResult.getAllPoi()) {
                mLatList.add(poiInfo.location);
                int distance = (int) getDistance(mLatLng.latitude, mLatLng.longitude, poiInfo.location.latitude, poiInfo.location.longitude);
                StationInfo stationInfo = new StationInfo();
                stationInfo.setBusNum(poiInfo.address);
                stationInfo.setName(poiInfo.name);
                stationInfo.setDistance(distance);
                mStationInfoList.add(stationInfo);
            }
            mQuickAdapter.notifyDataSetChanged();
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            Log.e(TAG, "onGetPoiDetailResult: " + poiDetailResult);
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };


    private String TAG = "-----";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //详情
            case R.id.sheet_details:
                Intent intent = new Intent(mActivity, StationDetailsActivity.class);
                intent.putExtra("busNuns", mStationInfoList.get(position).getBusNum());
                intent.putExtra("stationName", mStationInfoList.get(position).getName());
                startActivity(intent);
                break;

            case R.id.sheet_title:
                mRecyclerView.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(leverSmall, 300);
                mDialog.dismiss();
                break;
            case R.id.sheet_walk:
                routeWalk();

                break;
        }
    }

    private void routeWalk() {
        RoutePlanSearch mSearch;
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        PlanNode startNode = PlanNode.withLocation(mLatLng);
        PlanNode endNode = PlanNode.withLocation(mLatList.get(position));

        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(startNode).to(endNode));

//        mSearch.setOnGetRoutePlanResultListener(this);
//        mSearch.destroy();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mActivity, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            Log.e("======", "onGetWalkingRouteResult: " + result.error);
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            result.getSuggestAddrInfo();
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            if (result.getRouteLines().size() > 0) {
                // 直接显示
//                    route = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.e("route result", "结果数<0");
                return;
            }
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }


    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.e(TAG, "返回吗 " + location.getLocType());

            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                Log.e(TAG, "GPS定位: ");
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                searchStation();
                callLocation(location);
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                mDistrict = location.getDistrict();
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                searchStation();
                //当前为网络定位结果，可获取以下信息
                callLocation(location);
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                Log.e(TAG, "离线: " + location.getCity());
                //离线定位
                callLocation(location);
                mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                searchStation();
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                Log.e(TAG, "定位失败");
                //当前网络定位失败
                //可将定位唯一ID、IMEI、定位失败时间反馈至loc-bugs@baidu.com

            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                Log.e(TAG, "没网络 ");
                //当前网络不通

            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                Log.e(TAG, "没权限");
                //当前缺少定位依据，可能是用户没有授权，建议弹出提示框让用户开启权限
                //可进一步参考onLocDiagnosticMessage中的错误返回码
            }


        }
    }

    private boolean isFirst = true;

    private void callLocation(BDLocation location) {
        if (mMapView == null) {
            return;
        }
        MyLocationData locationData = new MyLocationData.Builder()
                .direction(currentX)
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locationData);
//        MyLocationConfiguration config = new MyLocationConfiguration(com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.FOLLOWING
//                , true,mBitmapDescriptor);
//        mBaiduMap.setMyLocationConfigeration(config);
        if (isFirst) {
            isFirst = false;
            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(mLatLng).zoom(18.5f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }


    public class QuickAdapter extends BaseQuickAdapter<StationInfo, BaseViewHolder> {
        public QuickAdapter() {
            super(R.layout.nearby_bus_item, NearbyFragment.this.mStationInfoList);
        }

        @Override
        protected void convert(BaseViewHolder helper, StationInfo item) {
            helper.setText(R.id.nearby_bus_name, item.getName());
            helper.setText(R.id.nearby_bus_bus, item.getBusNum());
            if (item.getDistance() < 100) {
                helper.setText(R.id.nearby_bus_distance, "<100m");
            } else {
                helper.setText(R.id.nearby_bus_distance, item.getDistance() + "m");
            }
        }
    }


}
