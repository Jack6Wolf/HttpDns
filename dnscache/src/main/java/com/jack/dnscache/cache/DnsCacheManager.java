package com.jack.dnscache.cache;

import android.content.Context;

import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.model.IpModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据缓存
 * 根据sp（或wifi名）缓存域名信息
 * 根据sp（或wifi名）缓存服务器ip信息、优先级
 * 记录服务器ip每次请求成功数、错误数
 * 记录服务器ip最后成功访问时间、最后测速
 * 添加 内存-》数据库 之间的缓存
 *
 * @version 1.0
 */
public class DnsCacheManager extends DNSCacheDatabaseHelper implements IDnsCache {

    /**
     * 延迟差值，单位s
     */
    public static int ip_overdue_delay = 20;
    /**
     * 缓存初始容量值
     */
    private final int INIT_SIZE = 8;
    /**
     * 缓存最大容量值
     */
    private final int MAX_CACHE_SIZE = 32;
    /**
     * 数据库操作类
     */
    private DNSCacheDatabaseHelper db = null;
    /**
     * 缓存链表
     */
    private ConcurrentHashMap<String, DomainModel> data = new ConcurrentHashMap<String, DomainModel>(INIT_SIZE, MAX_CACHE_SIZE);

    public DnsCacheManager(Context context) {
        super(context);
        db = new DNSCacheDatabaseHelper(context);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取url缓存
     *
     * @param url
     * @return
     */
    @Override
    public DomainModel getDnsCache(String sp, String url) {


        DomainModel model = data.get(url);

        if (model == null) {
            //缓存中没有从数据库中查找
            ArrayList<DomainModel> list = (ArrayList<DomainModel>) db.QueryDomainInfo(url, sp);
            if (list != null && list.size() != 0) {
                model = list.get(list.size() - 1);
            }

            //查询到数据 添加到缓存中
            if (model != null) addMemoryCache(url, model);

        }

        if (model != null) {
            //检测是否过期
            if (isExpire(model, ip_overdue_delay)) {
                model = null;
            }
        }

        return model;
    }


    /**
     * 插入本地 cache 缓存
     *
     * @param dnsPack
     * @return
     */
    @Override
    public DomainModel insertDnsCache(HttpDnsPack dnsPack) {

        DomainModel domainModel = new DomainModel();
        domainModel.domain = dnsPack.domain;
        domainModel.sp = dnsPack.localhostSp;
        domainModel.time = String.valueOf(System.currentTimeMillis());

        domainModel.ipModelArr = new ArrayList<IpModel>();

        int domainTTL = 60;
        for (HttpDnsPack.IP temp : dnsPack.dns) {

            IpModel ipModel = new IpModel();
            ipModel.ip = temp.ip;
            ipModel.ttl = temp.ttl;
            ipModel.priority = temp.priority;
            ipModel.source = temp.source;

            ipModel.port = 80;
            ipModel.sp = domainModel.sp;

            domainModel.ipModelArr.add(ipModel);
            //ip的ttl时间赋值给domain TTL
            domainTTL = Math.min(domainTTL, Integer.valueOf(ipModel.ttl));

        }
        //就是ip的ttl时间
        domainModel.ttl = String.valueOf(domainTTL);

        if (domainModel.ipModelArr.size() > 0) {
            // 插入数据库
            domainModel = super.addDomainModel(dnsPack.localhostSp, domainModel);
            // 插入内存缓存
            addMemoryCache(domainModel.domain, domainModel);
        }

        return domainModel;
    }

    /**
     * 获取即将过期的 domain 数据
     *
     * @return
     */
    @Override
    public ArrayList<DomainModel> getExpireDnsCache() {

        ArrayList<DomainModel> listDomain = new ArrayList<DomainModel>();

        for (Entry<String, DomainModel> entry : data.entrySet()) {
            DomainModel temp = data.get(entry.getKey());
            if (isExpire(temp)) {
                listDomain.add(temp);
            }
        }

        return listDomain;
    }


    /**
     * 获取缓存中全部的 DomainModel数据
     */
    @Override
    public ArrayList<DomainModel> getAllMemoryCache() {
        ArrayList<DomainModel> list = new ArrayList<DomainModel>();
        for (Entry<String, DomainModel> entry : data.entrySet()) {
            DomainModel temp = data.get(entry.getKey());
            list.add(temp);
        }
        return list;
    }

    /**
     * 清除全部缓存
     */
    @Override
    public void clear() {
        super.clear();
        data.clear();
    }

    /**
     * 清除内存缓存
     */
    @Override
    public void clearMemoryCache() {
        data.clear();
    }


    /**
     * 添加domain至缓存
     */
    @Override
    public void addMemoryCache(String domain, DomainModel model) {
        if (model.ipModelArr == null) return;
        if (model.ipModelArr.size() <= 0) return;
        for (IpModel ipModel : model.ipModelArr) {
            if (ipModel == null) return;
        }
        data.put(domain, model);
    }


    /**
     * 检测是否过期，提前3秒刷一下
     */
    private boolean isExpire(DomainModel domainModel) {
        return isExpire(domainModel, -3);
    }

    /**
     * 检测是否过期
     */
    private boolean isExpire(DomainModel domainModel, long difference) {
        long queryTime = Long.parseLong(domainModel.time) / 1000;
        long ttl = Long.parseLong(domainModel.ttl);
        long newTime = System.currentTimeMillis() / 1000;
        return (newTime - queryTime) > (ttl + difference);
    }

    @Override
    public void setSpeedInfo(List<IpModel> ipModels) {
        updateIpInfo(ipModels);
    }

}
