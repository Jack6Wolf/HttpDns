package com.jack.dnscache.score;

import com.jack.dnscache.model.IpModel;

import java.util.ArrayList;


public interface IPlugIn {

    /**
     * 插件实现计算分值的方法
     */
    public abstract void run(ArrayList<IpModel> list);

    /**
     * 权重
     */
    abstract float getWeight();

    /**
     * 是否开启该模块
     */
    abstract boolean isActivated();
}
