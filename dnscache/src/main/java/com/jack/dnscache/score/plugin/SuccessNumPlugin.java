package com.jack.dnscache.score.plugin;

import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.score.IPlugIn;
import com.jack.dnscache.score.PlugInManager;

import java.util.ArrayList;

/**
 * 结果导向为正
 * 计算原理和PriorityPlugin一样
 */
public class SuccessNumPlugin implements IPlugIn {

    @Override
    public void run(ArrayList<IpModel> list) {
        // 查找到最大历史成功次数
        float MAX_SUCCESSNUM = 0;
        for (IpModel temp : list) {
            if (temp.success_num == null || temp.success_num.equals(""))
                continue;
            float successNum = Float.parseFloat(temp.success_num);
            MAX_SUCCESSNUM = Math.max(MAX_SUCCESSNUM, successNum);
        }
        // 计算比值
        if (MAX_SUCCESSNUM == 0) {
            return;
        }
        float bi = getWeight() / MAX_SUCCESSNUM;
        // 计算得分
        for (IpModel temp : list) {
            if (temp.success_num == null || temp.success_num.equals("")) {
                continue;
            }
            float successNum = Float.parseFloat(temp.success_num);
            temp.grade += (successNum * bi);
        }

    }

    @Override
    public float getWeight() {
        return PlugInManager.SuccessNumPluginNum;
    }

    @Override
    public boolean isActivated() {
        return true;
    }

}
