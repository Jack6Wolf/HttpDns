package com.jack.dnscache.score;

import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.score.plugin.ErrNumPlugin;
import com.jack.dnscache.score.plugin.PriorityPlugin;
import com.jack.dnscache.score.plugin.SpeedTestPlugin;
import com.jack.dnscache.score.plugin.SuccessNumPlugin;
import com.jack.dnscache.score.plugin.SuccessTimePlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 评估算法插件
 * <p>
 * 本次测速 - 对ip组的每个ip测速打分
 * 官方推荐 - HttpDns接口 A记录中返回的优先级
 * 历史成功 - 该ip7天内成功访问次数
 * 历史错误 - 该ip7天内访问错误次数
 * 最后成功时间 - 该ip最后一次成功时间，阈值24
 */
public class PlugInManager {

    /**
     * 对每个IP打分，总分100分。
     * 本次测速 - 40分
     * 官方推荐 - 30分
     * 历史成功 - 10分
     * 历史错误 - 10分
     * 最后成功时间 - 10分
     * 总分=本次测速+官方推荐+历史成功次数+历史错误次数+最后成功时间
     * <p>
     * 目前权重分配完全基于主观认识，后期会根据建立的相应基线进行权重分配调整。
     * 使用者需要自己权衡，有可能随机的ip速度都好于权重打分的ip。
     */
    public static float SpeedTestPluginNum = 40;
    public static float PriorityPluginNum = 30;
    public static float SuccessNumPluginNum = 10;
    public static float ErrNumPluginNum = 10;
    public static float SuccessTimePluginNum = 10;
    public ArrayList<IPlugIn> plugIn = new ArrayList<IPlugIn>();


    public PlugInManager() {
        plugIn.add(new SpeedTestPlugin()); // 速度插件
        plugIn.add(new PriorityPlugin()); // 优先级推荐插件
        plugIn.add(new SuccessNumPlugin());//历史成功次数插件
        plugIn.add(new ErrNumPlugin()); // 历史错误次数插件
        plugIn.add(new SuccessTimePlugin());//最后一次成功时间插件
    }

    public void run(ArrayList<IpModel> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        //全部恢复得分为0（reset）
        for (IpModel temp : list) {
            if (null != temp) {
                temp.grade = 0;
            } else {
                return;
            }
        }
        for (IPlugIn plug : plugIn) {
            if (plug.isActivated()) {
                plug.run(list);
            }
        }
        ipModelSort(list);
    }

    public void ipModelSort(ArrayList<IpModel> list) {
        Collections.sort(list, new IpModelSort());
    }

    class IpModelSort implements Comparator<IpModel> {
        @Override
        public int compare(IpModel lhs, IpModel rhs) {
            return (int) (rhs.grade - lhs.grade);
        }
    }
}
