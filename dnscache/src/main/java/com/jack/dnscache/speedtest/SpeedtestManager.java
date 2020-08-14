package com.jack.dnscache.speedtest;

import com.jack.dnscache.Tools;
import com.jack.dnscache.speedtest.impl.PingTest;
import com.jack.dnscache.speedtest.impl.Socket80Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 测速类，供SpeedTestPlugin插件使用
 * <p>
 */
public class SpeedtestManager implements ISpeedtest {

    /**
     * 请求server过程中发生错误
     */
    public static final int OCUR_ERROR = -1;

    /**
     * 超时，无效的ip
     */
    public static final int MAX_OVERTIME_RTT = 0x9999;

    /**
     * 测速的轮询间隔
     */
    public static long time_interval = 60 * 1000;

    private CopyOnWriteArrayList<BaseSpeedTest> mSpeedTests = new CopyOnWriteArrayList<>();

    public SpeedtestManager() {
        mSpeedTests.add(new Socket80Test());//访问HttpServer80端⼝口，建⽴立连接
        mSpeedTests.add(new PingTest());//发送ICMP包，获取RTT值
    }

    /**
     * 测速，获取RTT耗时结果
     */
    @Override
    public int speedTest(String ip, String host) {
        Collections.sort(mSpeedTests, new Comparator<BaseSpeedTest>() {
            @Override
            public int compare(BaseSpeedTest lhs, BaseSpeedTest rhs) {
                if (lhs == null || rhs == null) {
                    return 0;
                } else {
                    // 按照降序排序
                    return rhs.getPriority() - lhs.getPriority();
                }
            }
        });

        for (BaseSpeedTest st : mSpeedTests) {
            Tools.log("TAG", "测速模块" + st.getClass().getSimpleName() + "启动," + "\n优先级是：" + st.getPriority() + "\n该模块是否开启：" + st.isActivate());
            if (st.isActivate()) {
                int rtt = st.speedTest(ip, host);
                Tools.log("TAG", "测速模块" + st.getClass().getSimpleName() + "结束," + "\n测速的结果是（RTT）：" + rtt);
                if (rtt > OCUR_ERROR) {
                    return rtt;
                }
            }
        }
        return OCUR_ERROR;
    }
}
