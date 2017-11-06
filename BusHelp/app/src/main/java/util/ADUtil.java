package util;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Hello on 2017/9/21.
 */

public class ADUtil {
    public ADUtil() {
        EventBus.getDefault().register(this);

    }

    private Context mContext;

    //获取广告信息
    public void requestAdertisement(Context context, final ImageView imageView) {
        mContext = context;
        HttpUtil.getInstance().request("https://update.androidsstore.com/libraryservers/AdvertismentServlet", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseData(response.body().string(),imageView);
            }
        });
    }

    String mAdImageUrl;
    String mAdUrl;
    String mAdName;

    private void responseData(String data, ImageView imageView) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String code = jsonObject.getString("code");
            if (!"200".equals(code)) {
                return;
            }
            mAdImageUrl = jsonObject.getString("adImageUrl");
            mAdUrl = jsonObject.getString("adUrl");
            mAdName = jsonObject.getString("adName");
            EventBus.getDefault().post(imageView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upUI(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadUtil downloadUtil = new DownloadUtil();
                downloadUtil.startDownload(mContext, mAdUrl, mAdName);

            }
        });

        if (mAdImageUrl.contains(".gif"))
            Glide.with(mContext).asGif().load(mAdImageUrl).into(imageView);
        else
            Glide.with(mContext).load(mAdImageUrl).into(imageView);

        unRegister();
    }


    public void unRegister() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

}
