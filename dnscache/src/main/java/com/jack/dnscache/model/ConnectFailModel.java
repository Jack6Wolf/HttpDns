/**
 *
 */
package com.jack.dnscache.model;


import com.jack.dnscache.cache.DBConstants;

/**
 * 链接异常数据模型 - 对应connect_fail表
 *
 * @version 1.0
 */
public class ConnectFailModel {

    /**
     * 链接失败表 自曾id <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_ID 字段 <br>
     */
    public int id = -1;
    /**
     * 链接失败的ip地址 <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_IP 字段 <br>
     */
    public int ip = -1;
    /**
     * 链接失败服务器的端口号 <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_PORT 字段 <br>
     */
    public int port = -1;
    /**
     * 链接失败的错误代码，（一般都是http的错误代码） <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_ERRCODE 字段 <br>
     */
    public String errcode = "";
    /**
     * 链接失败时本地网络类型 <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_NETWORK_TYPE 字段 <br>
     */
    public String network_type = "";
    /**
     * 链接失败时如果是手机运营商，则统计运营商sp—code（为了重现现场收集数据） <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_SPCODE 字段 <br>
     */
    public String spcode = "";
    /**
     * 链接该服务器总共的错误次数 <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_COUNT 字段 <br>
     */
    public String count = "";
    /**
     * 链接该服务器最后失败时间 <br>
     *
     * 该字段映射类 {@link DBConstants } CONNECT_FAIL_FINALLY_TIME 字段 <br>
     */
    public String finally_tiem = "";

    public ConnectFailModel() {
    }


}
