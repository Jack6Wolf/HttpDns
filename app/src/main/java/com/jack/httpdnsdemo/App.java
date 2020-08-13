package com.jack.httpdnsdemo;

import android.app.Application;

import com.jack.dnscache.DNSCache;
import com.jack.dnscache.DNSCacheConfig;

/**
 * @author jack
 * @since 2020/8/11 15:15
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DNSCache.Init(this);
        DNSCacheConfig.Data aDefault = DNSCacheConfig.Data.createDefault();
        DNSCacheConfig.saveLocalConfigAndSync(this, aDefault);
        DNSCache.getInstance().preLoadDomains(new String[]{"upms.startimestv.com"});
    }
}
