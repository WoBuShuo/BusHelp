package util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import view.UpdateVersionDialog;

/**
 * Created by Hello on 2017/9/21.
 */

public class VersionUtil {
    private String mUpDataLog;
    private String mUrl;
    private String mVersionSize;
    private String mVersionId;
    private Context mContext;
    public VersionUtil(){
        EventBus.getDefault().register(this);

    }

    public void requestData(Context context) {
        mContext=context;
        String versionCode = null;
        String versionName = null;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + "";
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        String postBody = ""
//                + "version_code=" + versionCode + "\n"
//                + "version_name=" + versionName + "\n";
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("version_code",versionCode);
        builder.add("version_name",versionName);
        RequestBody formBody = builder.build();
//        Request request = new Request.Builder().url("http://10.0.3.2:8080/text/AServlet").post(RequestBody.create(MEDIA_TYPE_MARKDOWN,
//                postBody)).build();
        Request request = new Request.Builder().url("https://update.androidsstore.com/libraryservers/AServlet").post(formBody).build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                analysisJson(response.body().string().trim());

            }
        });
    }

    /**
     * 解析是否有更新
     */
    private void analysisJson(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 400) {

            } else if (code == 200) {
                String newVersionUrl = jsonObject.getString("data");
                jsonObject = new JSONObject(newVersionUrl);
                mUpDataLog = jsonObject.getString("upDataLog");
                mUrl = jsonObject.getString("url");
                mVersionSize = jsonObject.getString("versionSize");
                mVersionId = jsonObject.getString("versionId");
            } else {

            }

            EventBus.getDefault().post(code);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void upUI(Integer code){
        if (code == 400) {
            Toast.makeText(mContext,"暫無新版本哦",Toast.LENGTH_SHORT).show();
        } else if (code == 200) {
            new UpdateVersionDialog().showDialog(mContext, mUpDataLog, mVersionId, mVersionSize,
                    new UpdateVersionDialog.ConfirmDialogListener() {
                        @Override
                        public void ok() {
                            Intent intent = new Intent(mContext, UpVersionService.class);
                            intent.putExtra("url", mUrl);
                            mContext. startService(intent);

//                            DownloadUtil downloadUtil = new DownloadUtil();
//                            downloadUtil.startDownload(mContext, mUrl, mVersionId);
                            Toast.makeText(mContext, "開始下載,請在下載完成後確認安裝！", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void cancel() {

                        }
                    });
        }
        unRegister();
    }
    public void unRegister(){
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }


}
