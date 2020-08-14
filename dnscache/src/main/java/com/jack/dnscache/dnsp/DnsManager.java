package com.jack.dnscache.dnsp;

import com.jack.dnscache.DNSCacheConfig;
import com.jack.dnscache.Tools;
import com.jack.dnscache.dnsp.impl.HttpPodDns;
import com.jack.dnscache.dnsp.impl.LocalDns;
import com.jack.dnscache.dnsp.impl.CustomHttpDns;
import com.jack.dnscache.dnsp.impl.UdpDns;
import com.jack.dnscache.log.HttpDnsLogManager;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.net.networktype.NetworkManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * dns解析
 * CustomHttpDns httpdns方式解析
 * HttpPodDns 依赖第三方dns服务商解析
 * UdpDns 直接udp方式去权威dns服务器解析
 * LocalDns 采用原生方式解析
 */
public class DnsManager implements IDns {

    CopyOnWriteArrayList<IDnsProvider> mDnsProviders = new CopyOnWriteArrayList<IDnsProvider>();
    private ArrayList<String> debugInfo = new ArrayList<String>();

    public DnsManager() {
        mDnsProviders.add(new CustomHttpDns()); //httpdns方式解析
        mDnsProviders.add(new HttpPodDns()); //依赖第三方dns服务商解析
        mDnsProviders.add(new UdpDns()); //udp方式去权威dns服务器解析
        mDnsProviders.add(new LocalDns()); //采用原生方式解析
    }

    /**
     * 根据优先级获取，谁先回来结果则终止该方法
     */
    @Override
    public HttpDnsPack requestDns(String domain) {
        Collections.sort(mDnsProviders, new Comparator<IDnsProvider>() {
            @Override
            public int compare(IDnsProvider lhs, IDnsProvider rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                } else {
                    // 按照降序排序
                    return rhs.getPriority() - lhs.getPriority();
                }
            }
        });
        for (IDnsProvider dp : mDnsProviders) {
            Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口开始," + "\n优先级是：" + dp.getPriority() + "\n该模块是否开启：" + dp.isActivate()
                    + "\n该模块的API地址是：" + dp.getServerApi());
            if (dp.isActivate()) {
                HttpDnsPack dnsPack = dp.requestDns(domain);
                Tools.log("TAG", "访问" + dp.getClass().getSimpleName() + "接口结束," + "\n返回的结果是：" + dnsPack);
                if (null != dnsPack) {
                    if (DNSCacheConfig.DEBUG) {
                        if (null != debugInfo) {
                            debugInfo.add(dnsPack.rawResult + "[from:" + dp.getClass().getSimpleName() + "]");
                        }
                    }
                    //获取wifi的ssid
                    dnsPack.localhostSp = NetworkManager.getInstance().getSPID();
                    if (!dnsPack.device_sp.equals(dnsPack.localhostSp)) {
                        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_SPINFO, dnsPack.toJson());
                    }

                    return dnsPack;
                }
            }
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_ERROR, HttpDnsLogManager.ACTION_ERR_DOMAININFO, "{\"domain\":" + "\"" + domain + "\"}");

        return null;
    }

    @Override
    public ArrayList<String> getDebugInfo() {
        return debugInfo;
    }

    @Override
    public void initDebugInfo() {
        debugInfo = new ArrayList<String>();
    }
}
