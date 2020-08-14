package com.jack.dnscache.score.plugin;

import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.score.IPlugIn;
import com.jack.dnscache.score.PlugInManager;

import java.util.ArrayList;

/**
 * 结果导向为正
 * 以最好那个ip为满分开始计算基准。
 * 假如3个ip优先级分别为 1,2,3
 * 则分数为： 10/3，20/3，10
 */
public class PriorityPlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        // 查找到最大优先级
        float MAX_PRIORITY = 0;
        for (IpModel temp : list) {
            if (temp.priority == null || temp.priority.equals(""))
                continue;
            float priority = Float.parseFloat(temp.priority);
            MAX_PRIORITY = Math.max(MAX_PRIORITY, priority);
        }
        // 计算比值
        if (MAX_PRIORITY == 0) {
            return;
        }
        float bi = getWeight() / MAX_PRIORITY;
        // 计算得分
        for (IpModel temp : list) {
            if (temp.priority == null || temp.priority.equals("")) {
                continue;
            }
            float priority = Float.parseFloat(temp.priority);
            temp.grade += (priority * bi);
        }

    }

    @Override
    public float getWeight() {
        return PlugInManager.PriorityPluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
