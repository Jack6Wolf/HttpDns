package com.jack.dnscache.speedtest.impl;

import android.text.TextUtils;

import com.jack.dnscache.speedtest.BaseSpeedTest;
import com.jack.dnscache.speedtest.SpeedtestManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 发送ICMP包，获取RTT值
 */
public class PingTest extends BaseSpeedTest {

    @Override
    public int speedTest(String ip, String host) {
        try {
            //-c 1:ping的次数 -s 1：计数跃点的时间戳  -w 1：等待每次回复的超时时间
            return Ping.runcmd("ping -c 1 " + ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return SpeedtestManager.OCUR_ERROR;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isActivate() {
        return true;
    }

    public static class Ping {
        private static final String TAG_BYTES_FROM = "bytes from ";

        public static int runcmd(String cmd) throws Exception {
            Runtime runtime = Runtime.getRuntime();
            Process proc = null;

            final String command = cmd.trim();
            long startTime = System.currentTimeMillis();
            proc = runtime.exec(command);
            proc.waitFor();
            long endTime = System.currentTimeMillis();
            InputStream inputStream = proc.getInputStream();
            String result = "unknown ip";

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder resultBuilder = new StringBuilder();
            String line = "";
            while (null != (line = reader.readLine())) {
                resultBuilder.append(line);
            }
            reader.close();
            String responseStr = resultBuilder.toString();
            result = responseStr.toLowerCase().trim();
            if (isValidResult(result)) {
                return (int) (endTime - startTime);
            }
            return SpeedtestManager.OCUR_ERROR;
        }

        private static boolean isValidResult(String result) {
            if (!TextUtils.isEmpty(result)) {
                if (result.indexOf(TAG_BYTES_FROM) > 0) {
                    return true;
                }
            }
            return false;
        }
    }
}
