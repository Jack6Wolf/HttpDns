/**
 *
 */
package com.jack.dnscache.dnsp;

import com.jack.dnscache.model.HttpDnsPack;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * httpdns server请求json解析
 *
 * @version 1.0
 */
public interface IJsonParser {

    public HttpDnsPack JsonStrToObj(String jsonStr) throws Exception;

    public class JavaJSON_HTTPDNS implements IJsonParser {

        @Override
        public HttpDnsPack JsonStrToObj(String jsonStr) throws Exception {
            HttpDnsPack dnsPack = new HttpDnsPack();
            JSONObject jsonObj = new JSONObject(jsonStr);
            dnsPack.rawResult = jsonStr;
            dnsPack.domain = jsonObj.getString("domain");
            dnsPack.device_ip = jsonObj.getString("device_ip");
            dnsPack.device_sp = jsonObj.getString("device_sp");

            JSONArray jsonarray = jsonObj.getJSONArray("dns");
            dnsPack.dns = new HttpDnsPack.IP[jsonarray.length()];
            for (int i = 0; i < dnsPack.dns.length; i++) {
                JSONObject tempJsonObj = new JSONObject(jsonarray.getString(i));
                dnsPack.dns[i] = new HttpDnsPack.IP();
                dnsPack.dns[i].ip = tempJsonObj.getString("ip");
                dnsPack.dns[i].ttl = tempJsonObj.getString("ttl");
                dnsPack.dns[i].priority = tempJsonObj.getString("priority");
            }
            return dnsPack;
        }
    }
}
