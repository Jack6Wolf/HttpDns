package com.jack.dnscache.score.plugin;

import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.score.IPlugIn;
import com.jack.dnscache.score.PlugInManager;

import java.util.ArrayList;

/**
 * 以24小时为阈值，最近成功时间超过24小时得分直接为0分
 * 24小时内的。时间越近分数越高
 */
public class SuccessTimePlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        final float dayTime = 24 * 60;// 单位是minute
        final float bi = getWeight() / dayTime;
        for (IpModel temp : list) {
            if (temp.finally_success_time == null || temp.finally_success_time.equals(""))
                continue;
            long lastSuccTime = Long.parseLong(temp.finally_success_time);
            long now = System.currentTimeMillis();
            long offTime = (now - lastSuccTime) / 1000 / 60; // 单位是minute
            if (offTime > dayTime) {
                continue;
            } else {
                temp.grade += (getWeight() - (offTime * bi));
            }
        }
    }

    @Override
    public float getWeight() {
        return PlugInManager.SuccessTimePluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
