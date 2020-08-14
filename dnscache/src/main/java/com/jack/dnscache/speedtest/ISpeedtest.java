package com.jack.dnscache.speedtest;

/**
 * Created by fenglei on 15/4/22.
 */
public interface ISpeedtest {

    /**
     * 返回RTT：即从发送端发送数据开始，到发送端收到来自接收端的确认（接收端收到数据后便立即发送确认），总共经历的时延。
     */
    public int speedTest(String ip, String host);

}
