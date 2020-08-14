package com.jack.dnscache.dnsp;

import com.jack.dnscache.model.HttpDnsPack;

import java.util.ArrayList;

/**
 * 各种Dns解析方式管理接口，按优先级排队解析
 */
public interface IDns {

    /**
     * 按优先级排队dns解析，谁先成功则终止
     */
    public HttpDnsPack requestDns(String domain);

    /**
     * debug调试信息
     */
    public ArrayList<String> getDebugInfo();

    /**
     * 初始化debug信息
     */
    public void initDebugInfo();

}