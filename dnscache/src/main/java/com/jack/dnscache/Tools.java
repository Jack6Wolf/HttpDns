package com.jack.dnscache;

import android.annotation.SuppressLint;
import android.util.Log;

import com.jack.dnscache.model.IpModel;

import org.json.JSONException;
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些常用的小方法
 *
 * @version 1.0
 */
public class Tools {

    /**
     * 从url中截取domain
     */
    public static String getHostName(String url) {
        if (url == null)
            return "";
        url = url.trim();
        String host = url.toLowerCase();
        int end;

        if (host.startsWith("http://")) {
            end = url.indexOf("/", 8);
            if (end > 7) {
                host = url.substring(7, end);
            } else {
                host = url.substring(7);
            }

        } else if (host.startsWith("https://")) {
            end = url.indexOf("/", 9);
            if (end > 8) {
                host = url.substring(8, end);
            } else {
                host = url.substring(8);
            }
        } else {
            end = url.indexOf("/", 1);
            if (end > 1) {
                host = url.substring(0, url.indexOf("/", 1));
            } else {
                host = url;
            }
        }
        return host;
    }


    /**
     * 转换url主机头为ip地址
     *
     * @param url  原url
     * @param host 主机头
     * @param ip   服务器ip
     * @return 变成ip+/login/user类型url
     */
    public static String getIpUrl(String url, String host, String ip) {

        if (url == null) {
            Tools.log("TAG", "URL NULL");
        }
        if (host == null) {
            Tools.log("TAG", "host NULL");
        }
        if (ip == null) {
            Tools.log("TAG", "ip NULL");
        }

        if (url == null || host == null || ip == null) return url;

        String ipUrl = url.replaceFirst(host, ip);

        return ipUrl;
    }

    public static String getStringDateShort(long time) {
        Date currentTime = new Date(time);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public static String getStringDateShort(String time) {
        return getStringDateShort(Long.parseLong(time));
    }

    /**
     * 全局日志
     */
    public static void log(String tag, String msg) {
        if (!DNSCacheConfig.DEBUG) return;
        Log.d(tag, msg);
    }

    /**
     * 随机排序算法
     *
     * @param list
     * @return
     */
    public static void randomSort(ArrayList<IpModel> list) {
        ArrayList<IpModel> temp = new ArrayList<IpModel>();
        temp.addAll(list);
        int count = temp.size();
        IpModel[] array = new IpModel[count];
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int num = rand.nextInt(count - i);
            array[i] = temp.remove(num);
        }
        int i = 0;
        // 下面这种写法，是为了不引起list的并发异常
        ListIterator<IpModel> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(array[i++]);
        }
    }

    /**
     * 判断该host是否是为 ipv4 格式。
     *
     * @param host 支持加端口验证
     * @return
     */
    public static boolean isIPV4(String host) {
        long begin = System.currentTimeMillis();
        host = host.trim();
        if (host.contains(":")) {
            String[] array = host.split(":");
            if (null != array && array.length == 2) {
                host = array[0];
                host = host.trim();
            }
        } else {
            if (null == host || host.length() < 7 || host.length() > 15 || "".equals(host)) {
                return false;
            }
        }
        String rexp = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." //
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." //
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."//
                + "(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(host);
        boolean ipAddress = mat.find();
        long end = System.currentTimeMillis();
        Tools.log("Tools:", "regular time spend : " + (end - begin) + "ms");
        return ipAddress;
    }

    /**
     * map转换成json
     */
    public String generateJsonStrFromMap(HashMap<String, String> map) {
        JSONStringer jsonStringer = new JSONStringer();
        try {
            jsonStringer = jsonStringer.object();
            Set<Entry<String, String>> entrySet = map.entrySet();
            for (Entry<String, String> entry : entrySet) {
                jsonStringer = jsonStringer.key(entry.getKey()).value(entry.getValue());
            }
            jsonStringer = jsonStringer.endObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return jsonStringer.toString();
    }

}
