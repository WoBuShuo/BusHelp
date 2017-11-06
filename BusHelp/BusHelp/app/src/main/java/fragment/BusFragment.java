package fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import com.hkbushelp.apps.R;
import com.hkbushelp.apps.RoutePlanActivity;
import com.hkbushelp.apps.SearchAddressActivity;
import util.HistoryBean;
import util.SqlHelper;
import util.VersionUtil;

/**
 * Created by Hello on 2017/8/24.
 */

public class BusFragment extends Fragment implements View.OnClickListener {

    private Activity mActivity;
    private TextView mStartAddress;
    private TextView mEndAddress;
    private SqlHelper mSqlHelper;
    private HistoryAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bus_fragment, null);
        mStartAddress = (TextView) view.findViewById(R.id.bus_fragment_start);
        mEndAddress = (TextView) view.findViewById(R.id.bus_fragment_end);
        Button button = (Button) view.findViewById(R.id.bus_fragment_search);
        ImageView exchange = (ImageView) view.findViewById(R.id.bus_fragment_exchange);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.bus_fragment_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        TextView delete= (TextView) view.findViewById(R.id.bus_fragment_delete);
        delete.setOnClickListener(this);


        exchange.setOnClickListener(this);
        mStartAddress.setOnClickListener(this);
        mEndAddress.setOnClickListener(this);
        button.setOnClickListener(this);
        mActivity = getActivity();

        //检查更新
        VersionUtil versionUtil=new VersionUtil();
        versionUtil.requestData(mActivity);


        selectSql();

        mAdapter = new HistoryAdapter();
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                HistoryBean historyBean = mHistoryBeanList.get(position);
                startRoutePlan(historyBean.getStartLatitude(), historyBean.getStartLongitude(),
                        historyBean.getEndLatitude(), historyBean.getEndLongitude(),
                        historyBean.getStart(), historyBean.getEnd());
            }
        });
        return view;
    }

    private List<HistoryBean> mHistoryBeanList;

    /**
     * 查询数据库，显示历史记录
     */
    private void selectSql() {
        mHistoryBeanList = new ArrayList<>();
        mSqlHelper = new SqlHelper(getActivity());
        SQLiteDatabase writableDatabase = mSqlHelper.getWritableDatabase();
        Cursor cursor = writableDatabase.query("History", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                HistoryBean historyBean = new HistoryBean();
                historyBean.setStart(cursor.getString(cursor.getColumnIndex("start_address")));
                historyBean.setEnd(cursor.getString(cursor.getColumnIndex("end_address")));
                historyBean.setStartLatitude(cursor.getString(cursor.getColumnIndex("start_latitude")));
                historyBean.setStartLongitude(cursor.getString(cursor.getColumnIndex("start_longitude")));
                historyBean.setEndLatitude(cursor.getString(cursor.getColumnIndex("end_latitude")));
                historyBean.setEndLongitude(cursor.getString(cursor.getColumnIndex("end_longitude")));
                mHistoryBeanList.add(historyBean);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private final int START_CODE = 34;
    private final int END_CODE = 33;
    public static final String HINT_STRING = "hint_string";

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bus_fragment_start:
                Intent intentStart = new Intent(getActivity(), SearchAddressActivity.class);
                intentStart.putExtra(HINT_STRING, "請輸入起點位置");
                startActivityForResult(intentStart, START_CODE);

                break;
            case R.id.bus_fragment_end:
                Intent intentEnd = new Intent(getActivity(), SearchAddressActivity.class);
                intentEnd.putExtra(HINT_STRING, "請輸入終點位置");
                startActivityForResult(intentEnd, END_CODE);
                break;
            case R.id.bus_fragment_search:
                boolean isRoute = startRoutePlan(mStartLatitude, mStartLongitude, mEndLatitude, mEndLongitude,
                        mStartAddress.getText().toString(), mEndAddress.getText().toString());
                //如果完善了信息，则将数据储存到数据库
                if (isRoute){
                    addData();
                }
                break;

            case R.id.bus_fragment_exchange:
                if (!"".equals(mStartLatitude) && !"".equals(mEndLatitude)) {
                    //交换位置信息
                    String endName = mEndAddress.getText().toString();
                    mEndAddress.setText(mStartAddress.getText().toString());
                    mStartAddress.setText(endName);
                    String startLongitude = mStartLongitude;
                    String startLatitude = mStartLatitude;
                    mStartLongitude = mEndLongitude;
                    mStartLatitude = mEndLatitude;
                    mEndLatitude = startLatitude;
                    mEndLongitude = startLongitude;
                } else {
                    Toast.makeText(getActivity(), "請完善位置", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.bus_fragment_delete:
                new AlertDialog.Builder(mActivity).setTitle("確認刪除歷史記錄").setCancelable(true)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase writableDatabase = mSqlHelper.getWritableDatabase();
                                writableDatabase.delete("History", null, null);
                                mHistoryBeanList.clear();
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", null).show();
                break;
        }
    }

    private void addData() {
        SQLiteDatabase writableDatabase = mSqlHelper.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("start_address",mStartAddress.getText().toString());
        values.put("end_address",mEndAddress.getText().toString());
        values.put("start_longitude",mStartLongitude);
        values.put("start_latitude",mStartLatitude);
        values.put("end_longitude",mEndLongitude);
        values.put("end_latitude",mEndLatitude);
        writableDatabase.insert("History",null,values);

        HistoryBean historyBean=new HistoryBean();
        historyBean.setStart(mStartAddress.getText().toString());
        historyBean.setEnd(mEndAddress.getText().toString());
        historyBean.setEndLongitude(mEndLongitude);
        historyBean.setEndLatitude(mEndLatitude);
        historyBean.setStartLongitude(mStartLongitude);
        historyBean.setStartLatitude(mStartLatitude);
        mHistoryBeanList.add(historyBean);
        mAdapter.notifyDataSetChanged();
    }

    /**
     *
     * 将数据传递到下个页面，进行搜索
     */
    private boolean startRoutePlan(String startLatitude, String startLongitude, String endLatitude,
                                String endLongitude, String startAddress, String endAddress) {
        if (!"".equals(startLatitude) && !"".equals(endLatitude)) {
            Intent intent = new Intent(getActivity(), RoutePlanActivity.class);
            intent.putExtra("start_longitude", startLongitude);
            intent.putExtra("start_latitude", startLatitude);
            intent.putExtra("end_longitude", endLongitude);
            intent.putExtra("start_name", startAddress);
            intent.putExtra("end_name", endAddress);
            intent.putExtra("end_latitude", endLatitude);
            startActivity(intent);
            return true;
        } else {
            Toast.makeText(getActivity(), "請完善位置", Toast.LENGTH_SHORT).show();
            return  false;
        }
    }


    class HistoryAdapter extends BaseQuickAdapter<HistoryBean, BaseViewHolder> {

        public HistoryAdapter() {
            super(R.layout.item_history, mHistoryBeanList);
        }

        @Override
        protected void convert(BaseViewHolder helper, HistoryBean item) {
            helper.setText(R.id.item_history_start, item.getStart());
            helper.setText(R.id.item_history_end, item.getEnd());
        }
    }


    private String mStartLongitude, mStartLatitude, mEndLongitude, mEndLatitude = "";


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == START_CODE && resultCode == Activity.RESULT_OK) {
            mStartAddress.setText(data.getStringExtra("address_name"));
            mStartLongitude = data.getStringExtra("address_longitude");
            mStartLatitude = data.getStringExtra("address_latitude");
        } else if (requestCode == END_CODE && resultCode == Activity.RESULT_OK) {
            mEndAddress.setText(data.getStringExtra("address_name"));
            mEndLongitude = data.getStringExtra("address_longitude");
            mEndLatitude = data.getStringExtra("address_latitude");
        }
    }
}