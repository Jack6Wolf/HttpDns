package com.jack.dnscache;


/**
 * 为使用者封装的数据模型
 *
 * @version 1.0
 */
public class DomainInfo {

    /**
     * id
     */
    public String id = null;
    /**
     * 可以直接使用的url  已经替换了host为ip
     */
    public String url = null;
    /**
     * A记录
     */
    public String ip = null;
    /**
     * 需要设置到 http head 里面的主机头
     */
    public String host = "";
    /**
     * 返回的内容体
     */
    public String data = null;
    /**
     * 开始请求的时间
     */
    public String startTime = null;
    /**
     * 请求结束的时间 请求超时 结束时间为null
     */
    public String stopTime = null;
    /**
     * 请求的状态值 200 \ 404 \ 500 等
     */
    public String code = null;

    /**
     * A记录来源，通过的途径
     * {@link com.jack.dnscache.dnsp.IDnsProvider#CUSTOMHTTPDNS}
     * {@link com.jack.dnscache.dnsp.IDnsProvider#HTTPPODDNS}
     * {@link com.jack.dnscache.dnsp.IDnsProvider#UDPDNS}
     * {@link com.jack.dnscache.dnsp.IDnsProvider#LOCALDNS}
     */
    public int source;


    /**
     * 构造函数
     *
     * @param id
     * @param url
     * @param host
     */
    public DomainInfo(String id, String ip, String url, String host,int source) {

        this.id = id;
        this.ip = ip;
        this.url = url;
        this.host = host;
        this.source = source;
        this.startTime = String.valueOf(System.currentTimeMillis());
    }

    /**
     * 工场方法
     */
    public static DomainInfo DomainInfoFactory(String ip, String url, String host,int source) {

        url = Tools.getIpUrl(url, host, ip);

        return new DomainInfo("", ip, url, host,source);
    }

    /**
     * 工场方法
     */
    public static DomainInfo[] DomainInfoFactory(String[] serverIpArray, String url, String host,int[] sources) {

        DomainInfo[] domainArr = new DomainInfo[serverIpArray.length];

        for (int i = 0; i < serverIpArray.length; i++) {

            domainArr[i] = DomainInfoFactory(serverIpArray[i], url, host,sources[i]);
        }

        return domainArr;
    }

    /**
     * 返回 url 信息
     */
    @Override
    public String toString() {

        String str = "DomainInfo: \n";
        str += "id = " + id + "\n";
        str += "url = " + url + "\n";
        str += "host = " + host + "\n";
        str += "source = " + source + "\n";
        str += "ip = " + ip + "\n";
        str += "data = " + data + "\n";
        str += "startTime = " + startTime + "\n";
        str += "stopTime = " + stopTime + "\n";
        str += "code = " + code + "\n";

        return str;
    }


}
