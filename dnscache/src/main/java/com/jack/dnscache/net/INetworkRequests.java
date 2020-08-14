/**
 *
 */
package com.jack.dnscache.net;

import java.util.HashMap;

/**
 * 由于该工程是一个辅助lib 尽量复用项目工程里在使用的网络库。
 *
 * @version 1.0
 */
public interface INetworkRequests {

    /**
     * 请求网络接口
     * @return 返回url数据内容
     */
    public String requests(String url);

    /**
     * 请求网络接口 (带host访问)
     *
     * @return 返回url数据内容
     */
    public String requests(String url, String host);

    /**
     * 请求网络接口 (多个head访问)
     *
     * @return 返回url数据内容
     */
    public String requests(String url, HashMap<String, String> head);

    /**
     * 请求网络接口 (多个head访问)
     *
     * @return 返回url数据内容
     */
    public byte[] requestsByteArr(String url, HashMap<String, String> head);

}
