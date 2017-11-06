package util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

public class PhoneInfo {
    private TelephonyManager telephonemanager;
    private String IMSI;
    private Context ctx;

    /**
     * 获取手机国际识别码IMEI
     */

    public PhoneInfo(Context context) {
        ctx = context;
        telephonemanager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }


    /**
     * 获取手机号码
     */
    public String getNativePhoneNumber() {
        String nativephonenumber = null;
            nativephonenumber = telephonemanager.getLine1Number();
        if (nativephonenumber == null || nativephonenumber.equals("")) {
            return "未获取到手机号码";
        }
        return nativephonenumber;
    }

    // 判断语言
    public String language() {
        String str = Locale.getDefault().getLanguage();
        String c = Locale.getDefault().getCountry();
        if (str == "zh" || str.equals("zh")) {
            if ("cn".equals(c) || "CN".equals(c)) {
                str = "中文（简）";
            } else if ("tw".equals(c) || "TW".equals(c)) {
                str = "中文（台湾繁体）";
            } else if ("hk".equals(c) || "HK".equals(c)) {
                str = "中文（香港繁体）";
            }
        } else if (str == "en" || str.equals("en")) {
            if ("uk".equals(c) || "UK".equals(c)) {
                str = "英文（英）";
            } else if ("us".equals(c) || "US".equals(c)) {
                str = "英文（美）";
            }
        } else if (str == "ja" || str.equals("ja")) {
            str = "日文";
        }
        return str;
    }

    /**
     * 判断国家是否是国内用户
     *
     * @return
     */
    public String isCN(Context context) {
        String countryIso = telephonemanager.getSimCountryIso();
        boolean isCN = false;// 判断是不是大陆
        String result = "";
        if (!TextUtils.isEmpty(countryIso)) {
            countryIso = countryIso.toUpperCase(Locale.US);
            if (countryIso.contains("CN")) {
                isCN = true;
                result = "国内";
            } else {
                result = "国外";
            }
        }
        return result;
    }

    /**
     * 获取联系人
     */
    public String getContact2() {
        String string = "";
        int count = 0;
        ContentResolver resolver = ctx.getContentResolver();

        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {
            // 取得联系人的名字索引
            int nameIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
            String contact = cursor.getString(nameIndex);
            string += contact + ":";
            // 取得联系人的ID索引值
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            // 查询该位联系人的电话号码，类似的可以查询email，photo
            Cursor phone = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);// 第一个参数是确定查询电话号，第三个参数是查询具体某个人的过滤值

            // 一个人可能有几个号码
            while (phone.moveToNext()) {
                String strPhoneNumber = phone
                        .getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                string = string + strPhoneNumber + " ;";
            }
            string += "\n";
            count++;
            phone.close();
        }
        cursor.close();
        return string;
    }

    /**
     * 获取通话记录
     */
    public String GetCallsInPhone() {
        String result = "";

        Cursor cursor = ctx.getContentResolver().query(Calls.CONTENT_URI,
                new String[]{Calls.DURATION, Calls.TYPE, Calls.DATE, Calls.NUMBER}, null, null,
                Calls.DEFAULT_SORT_ORDER);
        boolean hasRecord = cursor.moveToFirst();
        int count = 0;
        String strPhone = "";
        String date;

        while (hasRecord) {
            int type = cursor.getInt(cursor.getColumnIndex(Calls.TYPE));
            long duration = cursor.getLong(cursor.getColumnIndex(Calls.DURATION));
            strPhone = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date d = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(Calls.DATE))));
            date = dateFormat.format(d);
            result = result + strPhone + "  , ";
            result = result + date + "   ";
            result = result + " 通话时间" + duration + "秒,";

            switch (type) {
                case Calls.INCOMING_TYPE:
                    result = result + "  呼入";
                    break;
                case Calls.OUTGOING_TYPE:
                    result = result + "  呼出";
                    break;
                case Calls.MISSED_TYPE:
                    result = result + "  未接";
                    break;
                default:
                    break;
            }
            result += "\n\n";
            count++;
            hasRecord = cursor.moveToNext();
        }
        return result;
    }

    /**
     * 获取手机服务商信息
     */
    public String getProvidersName() {
        String providerName = null;
        try {
            IMSI = telephonemanager.getSubscriberId();
            // IMSI前面三位460是国家号码，其次的两位是运营商代号，00、02是中国移动，01是联通，03是电信。
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                providerName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                providerName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                providerName = "中国电信";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (providerName == null || providerName.equals("")) {
            return "未获取到运营商信息";
        }
        return providerName;

    }

    // 判断机器Android是否已经root，即是否获取root权限
    public String haveRoot() {
        int i = execRootCmdSilent("echo test"); // 通过执行测试命令来检测
        if (i != -1) {
            return "已Root";
        }
        return "未Root";
    }

    protected static int execRootCmdSilent(String paramString) {
        try {
            Process localProcess = Runtime.getRuntime().exec("su");
            Object localObject = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream((OutputStream) localObject);
            String str = String.valueOf(paramString);
            localObject = str + "\n";
            localDataOutputStream.writeBytes((String) localObject);
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();
            localProcess.waitFor();
            int result = localProcess.exitValue();
            return (Integer) result;
        } catch (Exception localException) {
            localException.printStackTrace();
            return -1;
        }
    }

    // MAC地址
    public String getLocalMac() {
        String mac = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    mac = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return mac;
    }

    /**
     * 获取IP地址
     *
     * @return
     */
    public String getHostIP() {

        String hostIp = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    /**
     * 获取手机信息
     */
    public String getPhoneInfo() {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append("\nDeviceID(IMEI)" + tm.getDeviceId());
        sb.append("\nDeviceSoftwareVersion:" + tm.getDeviceSoftwareVersion());
        sb.append("\ngetLine1Number:" + tm.getLine1Number());
        sb.append("\nNetworkCountryIso:" + tm.getNetworkCountryIso());
        sb.append("\nNetworkOperator:" + tm.getNetworkOperator());
        sb.append("\nNetworkOperatorName:" + tm.getNetworkOperatorName());
        sb.append("\nNetworkType:" + tm.getNetworkType());
        sb.append("\nPhoneType:" + tm.getPhoneType());
        sb.append("\nSimCountryIso:" + tm.getSimCountryIso());
        sb.append("\nSimOperator:" + tm.getSimOperator());
        sb.append("\nSimOperatorName:" + tm.getSimOperatorName());
        sb.append("\nSimSerialNumber:" + tm.getSimSerialNumber());
        sb.append("\ngetSimState:" + tm.getSimState());
        sb.append("\nSubscriberId:" + tm.getSubscriberId());
        sb.append("\nVoiceMailNumber:" + tm.getVoiceMailNumber());
        // return sb.toString();
        return tm.getDeviceId();

    }
}