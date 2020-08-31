package com.jack.dnscache.dnsp.impl;

import com.jack.dnscache.dnsp.IDnsProvider;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.net.networktype.NetworkManager;

import java.net.InetAddress;

/**
 * LocalDns的方式做Dns解析{@link InetAddress#getAllByName(String)}
 */
public class LocalDns implements IDnsProvider {

    @Override
    public HttpDnsPack requestDns(String domain) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            String[] ipList = new String[addresses.length];
            for (int i = 0; i < addresses.length; i++) {
                ipList[i] = addresses[i].getHostAddress();
            }
            if (ipList.length > 0) {
                HttpDnsPack dnsPack = new HttpDnsPack();
                String IPArr[] = ipList;
                //统一默认给60s ttl时间
                String TTL = "60";
                dnsPack.domain = domain;
                dnsPack.device_ip = NetworkManager.Util.getLocalIpAddress();
                dnsPack.device_sp = NetworkManager.getInstance().getSPID() ;
                dnsPack.rawResult = "domain:" + domain + ";\nipArray:";
                dnsPack.dns = new HttpDnsPack.IP[IPArr.length];
                for (int i = 0; i < IPArr.length; i++) {
                    String ip = IPArr[i];
                    if (i == IPArr.length - 1) {
                        //去掉最后的逗号
                        dnsPack.rawResult += (ip);
                    } else {
                        dnsPack.rawResult += (ip + ",");
                    }
                    dnsPack.dns[i] = new HttpDnsPack.IP();
                    dnsPack.dns[i].ip = ip;
                    dnsPack.dns[i].ttl = TTL;
                    dnsPack.dns[i].priority = "0";
                    dnsPack.dns[i].source = IDnsProvider.LOCALDNS;
                }
                return dnsPack;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isActivate() {
        return true;
    }

    @Override
    public String getServerApi() {
        return null;
    }

}
