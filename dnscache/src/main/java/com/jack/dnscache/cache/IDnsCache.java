package com.jack.dnscache.cache;

import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.model.IpModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存层对外接口
 *
 * @version 1.0
 */
public interface IDnsCache {

    /**
     * 获取 domain 缓存
     *
     * @param sp
     * @param domain
     * @return
     */
    public DomainModel getDnsCache(String sp, String domain);


    /**
     * 插入一条缓存记录
     *
     * @param dnsPack
     * @return
     */
    public DomainModel insertDnsCache(HttpDnsPack dnsPack);


    /**
     * 批量更新测速后信息，保证事务的原子性。即保持一个域名下的所有ip都同时修改。
     *
     * @param ipModels
     */
    public void setSpeedInfo(List<IpModel> ipModels);


    /**
     * 获取即将过期的domain信息
     *
     * @return
     */
    public ArrayList<DomainModel> getExpireDnsCache();

    /**
     * 内存中 增加缓存信息
     *
     * @param url
     * @param model
     */
    public void addMemoryCache(String url, DomainModel model);


    /**
     * 清除全部缓存数据
     */
    public void clear();


    /**
     * 清除内存缓存
     */
    public void clearMemoryCache();

    /**
     * 获取缓存中全部的 DomainModel数据
     *
     * @return
     */
    public ArrayList<DomainModel> getAllMemoryCache();


    /**
     * 获取数据库 domain 表，默认不追加ip表的数据
     */
    public ArrayList<DomainModel> getAllTableDomain();

    public ArrayList<DomainModel> getAllTableDomain(boolean appendIpinfo);


    /**
     * 获取数据库 ip 表
     */
    public ArrayList<IpModel> getTableIP();

}
