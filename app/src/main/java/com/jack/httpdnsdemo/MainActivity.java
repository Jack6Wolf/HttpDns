package com.jack.httpdnsdemo;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.jack.dnscache.DNSCache;
import com.jack.dnscache.DomainInfo;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

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

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                localdns();
//            }
//        }).start();

    }

    private void localdns() {
        try {
            InetAddress[] allByName = InetAddress.getAllByName("www.baidu.com");
            for (InetAddress inetAddress : allByName) {
                Log.e(TAG,inetAddress.getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getIp() {
        DomainInfo[] infoList = DNSCache.getInstance().getDomainServerIp("http://www.baidu.com/ums/v4/user/login");
        if (infoList != null) {
            for (DomainInfo domainInfo : infoList) {
                Log.e("MainActivity", domainInfo.ip);
            }
        }
    }
}