package com.jack.dnscache.net.networktype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.jack.dnscache.DNSCache;

/**
 * 网络有变化的时候，会刷新 NetworkManager类中的网络环境，在客户端内如果是⼿机网络可以知道网络类型（2G、3G、4G）
 * 也可以知道当前SP（移动、联通、电信），如果是Wifi网络环境可以知道SSID（wifi名字）在刷新网络环境后，会重新查询缓
 * 存内是否有当前链路下的最优A记录，如果没有则从LocalDNS获取第⼀次，然后⻢上更新httpdns记录。
 * <p>
 * 数据库中缓存的数据，是根据当前sp来缓存的，也就是说当⾃⾝网络环境变化后，返回的a记录是不⼀样的 。
 * ⼿机网络下会根据当前sp来缓存 a记录服务器ip，如果是wifi网络环境下 根据当前ssid来缓存a记录，
 * 因为wifi环境下库⾃⼰没有办法明确判断出⾃⼰的运营商，但相同的ssid不会发⽣频繁的网络运营商
 * 变化。 所以在wifi下请求回来的a记录直接关联ssid名字即可，即使wifi sp发⽣变化，最多延迟⼀个ttl时
 * 间就更新成最新的a记录了。
 */
public class NetworkStateReceiver extends BroadcastReceiver {

    public String TAG = "TAG_NET";

    public static NetworkInfo getActiveNetwork(Context context) {
        if (context == null)
            return null;
        ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnMgr == null)
            return null;
        NetworkInfo aActiveInfo = mConnMgr.getActiveNetworkInfo(); // 获取活动网络连接信息
        return aActiveInfo;
    }

    public static void register(Context context) {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(new NetworkStateReceiver(), mFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = getActiveNetwork(context);
            if (networkInfo != null) {
                // 刷新网络环境
                if (NetworkManager.getInstance() != null) {
                    NetworkManager.getInstance().Init();
                    if (DNSCache.getInstance() != null) {
                        DNSCache.getInstance().onNetworkStatusChanged(networkInfo);
                    }
                }
            }
        }
    }
}
