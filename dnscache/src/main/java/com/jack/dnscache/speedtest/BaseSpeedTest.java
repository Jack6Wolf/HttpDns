package com.jack.dnscache.speedtest;


public abstract class BaseSpeedTest {

    public abstract int speedTest(String ip, String host);

    /**
     * 被执行的优先级
     */
    public abstract int getPriority();

    /**
     * 是否开启该空间
     */
    public abstract boolean isActivate();

}