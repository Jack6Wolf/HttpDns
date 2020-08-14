package com.jack.dnscache.query;

import com.jack.dnscache.cache.IDnsCache;
import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.speedtest.SpeedtestManager;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * 查询模块管理类
 * <p>
 * 检测本地是否有相应的域名缓存
 * 没有记录则根据当前运营商返回内置的ip节点
 * 从httpdns查询域名相应A记录，缓存域名信息
 * 查询模块必须保证响应速度，基于已有设备测试平均在5毫秒左右
 *
 * @version 1.0
 */
public class QueryManager implements IQuery {

    private IDnsCache dnsCache = null;

    public QueryManager(IDnsCache dnsCache) {
        this.dnsCache = dnsCache;
    }

    /**
     * 根据host名字查询server ip
     */
    @Override
    public DomainModel queryDomainIp(String sp, String host) {

        // 从缓存中查询，如果为空 情况有两种 1：没有缓存数据 2：数据过期
        DomainModel domainModel = getCacheDomainIp(sp, host);

        // 如果缓存是无效数据，则取localdns返回
        if (inValidData(domainModel)) {

            String[] ipList = null;
            try {
                // FIXME: 2020/8/14 最好移到工作线程执行
                InetAddress[] addresses = InetAddress.getAllByName(host);
                ipList = new String[addresses.length];
                for (int i = 0; i < addresses.length; i++) {
                    ipList[i] = addresses[i].getHostAddress();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != ipList) {
                domainModel = new DomainModel();
                domainModel.id = -1;
                domainModel.domain = host;
                domainModel.sp = sp;
                domainModel.ttl = "60";
                domainModel.time = String.valueOf(System.currentTimeMillis());
                domainModel.ipModelArr = new ArrayList<IpModel>();
                for (int i = 0; i < ipList.length; i++) {
                    domainModel.ipModelArr.add(new IpModel());
                    domainModel.ipModelArr.get(i).ip = ipList[i];
                    domainModel.ipModelArr.get(i).sp = sp;
                }
                dnsCache.addMemoryCache(host, domainModel);
            }
        }
        return domainModel;
    }

    /**
     * 是否是无效数据。判断依据：
     * 1.domainModel为null
     * 2.domainModel.ipModelArr == null
     * 3.domainModel.ipModelArr.size() == 0
     * 4.domainModel.ipModelArr的rtt都是计算都出错，即都不通
     */
    private boolean inValidData(DomainModel domainModel) {
        if (domainModel == null || domainModel.ipModelArr == null || domainModel.ipModelArr.size() == 0) {
            return true;
        }
        ArrayList<IpModel> ips = domainModel.ipModelArr;
        for (IpModel ipModel : ips) {
            //只要有一个是通的，就认为是有效数据
            if (!("" + SpeedtestManager.MAX_OVERTIME_RTT).equals(ipModel.rtt)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 从缓存层获取获取数据
     */
    @Override
    public DomainModel getCacheDomainIp(String sp, String host) {
        return dnsCache.getDnsCache(sp, host);
    }
}
