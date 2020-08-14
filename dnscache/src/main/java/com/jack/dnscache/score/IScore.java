package com.jack.dnscache.score;

import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.IpModel;

import java.util.ArrayList;

/**
 * 排序接口
 */
public interface IScore {

    /**
     * 通过排序插件计算ip得分
     */
    public String[] serverIpScore(DomainModel domainModel);

    /**
     * 拼接ip
     */
    public String[] ListToArr(ArrayList<IpModel> list);
}
