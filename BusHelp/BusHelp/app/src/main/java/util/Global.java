package util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by Hello on 2017/8/28.
 */

public class Global {

    private static int mRandomNum=13;
    public static final String ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String BASE_URL = "http://mobile.nwstbus.com.hk/api6/";


    /**
     * 返回的是搜索某地址的网址
     */
    public static String getSearchStation(String stationName) {
        String code = generateString(mRandomNum);

        return BASE_URL + "bsearch.php?k=" + stationName
                + "&l=2&syscode=" + code +
                "&p=android";
    }

    /**
     * @return 返回某地到某地的请求网址
     */
    public static String getRoutePlanUrl(String startLatitude, String startLongitude,
                                         String endLatitude, String endLongitude) {
        String code = generateString(mRandomNum);
        return BASE_URL + "p2psearch.php?sp=" + startLongitude + "," + startLatitude + "&ep=" + endLongitude + "," +
                 endLatitude+ "&m1=T&m2=2&d=400&t=" + getTime() + "&tm=D&ws=1.3&l=2&syscode=" + code + "&p=android";
//        return BASE_URL + "p2psearch.php?sp=" + startLongitude + "," + startLatitude + "&ep=" + endLongitude + "," +
//                endLatitude+ "&m1=T&m2=2&d=400&t=" +"2017-09-22+23%3A28"  + "&tm=D&ws=1.3&l=2&syscode=" + code + "&p=android";

    }

    /**
     * @return 返回换乘详情的请求地址
     */
    public static String getPlanDetails(String info){
        return BASE_URL+"ppstoplist.php?info="+info+"&l=2&syscode="+generateString(10)+"&p=android";
    }


    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALLCHAR.charAt(random.nextInt(ALLCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * @return 返回当前时间
     */
    public static String getTime() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd+HH:mm");//可以方便地修改日期格式
        return dateFormat.format(now);
    }

    public static final String BASE_8684="http://hongkong.8684.cn";
    /**
     * @return 返回搜索某线路的地址
     */
    public static String getSearch8684BusNum(String busNum) {
        return BASE_8684+"/so.php?k=pp&q="+busNum;
    }

    public static String getSearch8684Station(String stationName){
        return BASE_8684+"/auto.php?keys="+stationName;
    }



    /**
     * 获取屏幕的高度px
     *
     * @param context 上下文
     * @return 屏幕高px
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        //设置宽高
        return d.heightPixels;
    }


    /**
     * 权限常量
     */
    public static final int WRITE_READ_EXTERNAL_CODE=0x01;
    public static final int ACCESS_FINE_LOCATION_CODE=0x02;
    public static final int ACCESS_COARSE_LOCATION_CODE=0x03;



}
