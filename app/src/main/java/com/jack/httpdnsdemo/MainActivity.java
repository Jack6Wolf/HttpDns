package com.jack.httpdnsdemo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.dnscache.DNSCache;
import com.jack.dnscache.DomainInfo;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    getIp();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        getIp();

    }

    private void getIp() {
        DomainInfo[] infoList = DNSCache.getInstance().getDomainServerIp("http://qa.upms.startimestv.com/ums/v4/user/login");
        if (infoList != null) {
            for (DomainInfo domainInfo : infoList) {
                Log.e("MainActivity", domainInfo.url);
            }
        }
    }
}