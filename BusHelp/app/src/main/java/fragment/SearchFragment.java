package fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hkbushelp.apps.BusDetailsActivity;
import com.hkbushelp.apps.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.Global;
import util.HttpUtil;

/**
 * Created by Hello on 2017/8/24.
 */

public class SearchFragment extends Fragment {

    private SearchAdapter mAdapter;
    private EditText mEditText;
    private Activity mActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, null);
        mActivity = getActivity();

        mEditText = (EditText) view.findViewById(R.id.search_fragment_edit);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.search_fragment_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mAdapter = new SearchAdapter(mNameList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity,
                DividerItemDecoration.VERTICAL));
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(mActivity, BusDetailsActivity.class);
                intent.putExtra("busUrl", mUrlList.get(position));
                intent.putExtra("busNum", mNameList.get(position));
                startActivity(intent);
            }
        });

        requestHotNet();


        ImageButton searchBtn = (ImageButton) view.findViewById(R.id.search_fragment_search_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSearchNet();
            }
        });

        return view;
    }

    private void requestHotNet() {
        HttpUtil.getInstance().request(Global.BASE_8684, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                splitHotResult(response.body().string());
            }
        });
    }

    private final int HOT_CALL = 41;
    private final int SEARCH_CALL = 42;
    private final int AD_CALL = 43;

    private void splitHotResult(String result) {
        Document document = Jsoup.parseBodyFragment(result);
        Element layerElement = document.select("div.bus_layer").first();
        Elements contentElements = layerElement.select("div.bus_layer_content");
        Elements a = contentElements.get(3).select("a");
        for (Element element : a) {
            mNameList.add(element.text());
            mUrlList.add(Global.BASE_8684 + element.attr("href"));
        }
        mHandler.sendEmptyMessage(HOT_CALL);
    }

    /**
     * 请求搜索的网络请求
     */
    private void requestSearchNet() {

        String url = "";
        url = Global.getSearch8684BusNum(mEditText.getText().toString());

        HttpUtil.getInstance().request(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                splitBusNumResult(response.body().string());
            }
        });
    }


    private List<String> mUrlList = new ArrayList<>();
    private List<String> mNameList = new ArrayList<>();

    //解析搜索巴士号码的数据
    private void splitBusNumResult(String result) {
        mUrlList.clear();
        mNameList.clear();
        Document document = Jsoup.parseBodyFragment(result);

        Element leftElement = document.select("div.cc_content").first();
        Elements a = leftElement.select("a");
        for (Element element : a) {
            mUrlList.add(Global.BASE_8684 + element.attr("href"));
            mNameList.add(element.text());
        }
        mHandler.sendEmptyMessage(SEARCH_CALL);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HOT_CALL:
                    mAdapter.notifyDataSetChanged();
                    break;

                case SEARCH_CALL:
                    mAdapter.notifyDataSetChanged();
                    break;

                case AD_CALL:

                    break;
            }

        }
    };


    class SearchAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public SearchAdapter(@Nullable List<String> data) {
            super(R.layout.item_search_fragment, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.item_search_fragment_text, item);
        }
    }





}
