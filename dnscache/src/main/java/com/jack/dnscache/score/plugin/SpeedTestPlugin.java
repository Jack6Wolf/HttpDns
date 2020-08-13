package com.jack.dnscache.score.plugin;

import java.util.ArrayList;

import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.score.IPlugIn;
import com.jack.dnscache.score.PlugInManager;

public class SpeedTestPlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        // 查找到最大速度
        float MAX_SPEED = 0;
        for (IpModel temp : list) {
            if (temp.rtt == null || temp.rtt.equals(""))
                continue;
            float finallySpeed = Float.parseFloat(temp.rtt);
            MAX_SPEED = Math.max(MAX_SPEED, finallySpeed);
        }
        // 计算比值
        if (MAX_SPEED == 0) {
            return;
        }
        float bi = getWeight() / MAX_SPEED;
        // 计算得分
        for (IpModel temp : list) {
            if (temp.rtt == null || temp.rtt.equals("")){
                continue;
            }
            float finallySpeed = Float.parseFloat(temp.rtt);
            temp.grade += (getWeight() - (finallySpeed * bi));
        }
    }

    @Override
    public float getWeight() {
        return PlugInManager.SpeedTestPluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
