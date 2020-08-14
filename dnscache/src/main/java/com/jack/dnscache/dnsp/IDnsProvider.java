package com.jack.dnscache.dnsp;

import com.jack.dnscache.model.HttpDnsPack;

/**
 * 多种dns解析暴露接口
 *
 * @see com.jack.dnscache.dnsp.impl.CustomHttpDns
 * @see com.jack.dnscache.dnsp.impl.HttpPodDns
 * @see com.jack.dnscache.dnsp.impl.LocalDns
 * @see com.jack.dnscache.dnsp.impl.UdpDns
 */
public interface IDnsProvider {

    /**
     * 请求dns server，返回指定的域名解析信息
     */
    public HttpDnsPack requestDns(String domain);

    /**
     * 被执行的优先级
     */
    public int getPriority();

    /**
     * 是否是激活状态
     */
    public boolean isActivate();


    /**
     * 获取dns server的地址
     */
    public String getServerApi();
}