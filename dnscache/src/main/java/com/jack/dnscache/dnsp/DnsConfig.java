package com.jack.dnscache.dnsp;

import java.util.ArrayList;

/**
 * 各种DNS解析接口配置类
 *
 * @version 1.0
 */
public class DnsConfig {

    /**
     * HTTPDNS 开关
     */
    public static boolean enableHttpDns = true;

    /**
     * DNSPOD 开关
     */
    public static boolean enableDnsPod = true;

    /**
     * UDPDNS 开关
     */
    public static boolean enableUdpDns = true;

    /**
     * HTTPDNS 服务器地址
     */
    public static ArrayList<String> HTTPDNS_SERVER_API = new ArrayList<String>();

    /**
     * DNSPOD 服务器地址
     */
    public static String DNSPOD_SERVER_API = "";

    /**
     * UDPDNS 服务器地址
     */
    public static String UDPDNS_SERVER_API = "";


}
