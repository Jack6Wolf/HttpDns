package com.jack.dnscache.dnsp.impl;

import android.text.TextUtils;

import com.jack.dnscache.dnsp.DnsConfig;
import com.jack.dnscache.dnsp.IDnsProvider;
import com.jack.dnscache.dnsp.IJsonParser.JavaJSON_HTTPDNS;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.net.ApacheHttpClientNetworkRequests;
import com.jack.dnscache.net.INetworkRequests;

import java.util.ArrayList;

/**
 * 使用自定义httpdns服务器拉取ip
 */
public class CustomHttpDns implements IDnsProvider {

    private ApacheHttpClientNetworkRequests netWork;
    private JavaJSON_HTTPDNS jsonObj;
    private String usingServerApi = "";

    public CustomHttpDns() {
        netWork = new ApacheHttpClientNetworkRequests();
        jsonObj = new JavaJSON_HTTPDNS();
    }

    @Override
    public HttpDnsPack requestDns(String domain) {
        String jsonDataStr = null;
        HttpDnsPack dnsPack = null;
        ArrayList<String> serverApis = new ArrayList<String>();
        serverApis.addAll(DnsConfig.HTTPDNS_SERVER_API);
        while (serverApis.size() > 0) {
            try {
                String api = "";
                int index = serverApis.indexOf(usingServerApi);
                if (index != -1) {
                    api = serverApis.remove(index);
                } else {
                    api = serverApis.remove(0);
                }
                String httpdns_api_url = api + domain;
                jsonDataStr = netWork.requests(httpdns_api_url, INetworkRequests.METHOD_GET);
                dnsPack = jsonObj.JsonStrToObj(jsonDataStr);
                usingServerApi = api;
                if (dnsPack != null && dnsPack.dns != null && dnsPack.dns.length > 0)
                    return dnsPack;
            } catch (Exception e) {
                e.printStackTrace();
                usingServerApi = "";
            }
        }
        return null;
    }

    @Override
    public boolean isActivate() {
        return DnsConfig.enableHttpDns;
    }


    @Override
    public String getServerApi() {
        String serverApi = "";
        if (!TextUtils.isEmpty(usingServerApi)) {
            serverApi = usingServerApi;
        } else {
            boolean yes = DnsConfig.HTTPDNS_SERVER_API.size() > 0;
            if (yes) {
                serverApi = DnsConfig.HTTPDNS_SERVER_API.get(0);
            }
        }
        return serverApi;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
