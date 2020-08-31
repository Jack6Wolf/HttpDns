/**
 *
 */
package com.jack.dnscache.model;

import org.json.JSONException;
import org.json.JSONStringer;

/**
 * 将httpdns返回的数据封装一层，方便日后httpdns接口改动不影响数据库模型。
 * 并且该接口还会标识httpdns错误之后的一些信息用来上报
 * 格式：{
 *     "domain": "api.weibo.cn",
 *     "device_ip": "10.209.70.192",
 *     "device_sp": "0",
 *     "dns": [
 *         {
 *             "priority": "0",
 *             "ip": "123.125.105.231",
 *             "ttl": "60"
 *         },
 *         {
 *             "priority": "0",
 *             "ip": "123.125.105.246",
 *             "ttl": "60"
 *         },
 *         {
 *             "priority": "0",
 *             "ip": "202.108.7.133",
 *             "ttl": "60"
 *         }
 *     ]
 * }
 */
public class HttpDnsPack {

    /**
     * httpdns 接口返回字段 域名信息
     */
    public String domain = "";

    /**
     * httpdns 接口返回字段 请求的设备ip（也可能是sp的出口ip）
     */
    public String device_ip = "";

    /**
     * httpdns 接口返回字段 请求的设备sp运营商
     */
    public String device_sp = "";

    /**
     * httpdns 接口返回的a记录。（目前不包含cname别名信息）
     */
    public IP[] dns = null;

    /**
     * 本机识别的sp运营商，手机卡下运营商正常，wifi下为ssid名字
     */
    public String localhostSp = "";

    /**
     * httpdns 接口返回的原始信息
     */
    public String rawResult;

    /**
     * 打印该类相关变量信息
     */
    @Override
    public String toString() {

        String str = "HttpDnsPack class \n";
        str += "domain:" + domain + "\n";
        str += "device_ip:" + device_ip + "\n";
        str += "device_sp:" + device_sp + "\n";

        if (dns != null) {
            str += "-------------------\n";
            for (int i = 0; i < dns.length; i++) {
                str += "dns[" + i + "]:" + dns[i] + "\n";
            }
            str += "-------------------\n";
        }

        return str;
    }

    public String toJson() {
        JSONStringer jsonStringer = new JSONStringer();
        try {
            StringBuilder ipArrayStr = new StringBuilder();
            ipArrayStr.append("[");
            if (null != dns) {
                for (IP ip : dns) {
                    ipArrayStr.append(ip.toJson() + ",");
                }
            }
            if (ipArrayStr.toString().endsWith(",")) {
                ipArrayStr.deleteCharAt(ipArrayStr.length() - 1);
            }
            ipArrayStr.append("]");

            jsonStringer.object()//
                    .key("domain").value(domain)//
                    .key("device_ip").value(device_ip)//
                    .key("device_sp").value(device_sp)//
                    .key("localhostSp").value(localhostSp)//
                    .key("rawResult").value(rawResult)//
                    .key("ipArray").value(ipArrayStr.toString())//
                    .endObject();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
        return jsonStringer.toString();
    }

    /**
     * A记录相关字段信息
     */
    public static class IP {

        /**
         * A记录IP
         */
        public String ip = "";

        /**
         * 域名A记录过期时间
         */
        public String ttl = "";

        /**
         * 服务器推荐使用的A记录 级别从0-10
         */
        public String priority = "";
        public int source;


        /**
         * 打印该类信息
         */
        @Override
        public String toString() {
            String str = "IP class \n";
            str += "ip:" + ip + "\n";
            str += "ttl:" + ttl + "\n";
            str += "priority:" + priority + "\n";
            str += "source:" + source + "\n";
            return str;
        }

        public String toJson() {
            JSONStringer jsonStringer = new JSONStringer();
            try {
                jsonStringer.object()//
                        .key("ip").value(ip)//
                        .key("ttl").value(ttl)//
                        .key("priority").value(priority)//
                        .key("source").value(source)//
                        .endObject();
            } catch (JSONException e) {
                e.printStackTrace();
                return "{}";
            }
            return jsonStringer.toString();
        }

    }

}
