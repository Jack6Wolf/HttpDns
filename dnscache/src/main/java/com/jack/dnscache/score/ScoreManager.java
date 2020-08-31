package com.jack.dnscache.score;

import com.jack.dnscache.Tools;
import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.IpModel;

import java.util.ArrayList;

/**
 * 对 ip 进行排序
 * <p>
 * 根据本地数据，对一组ip排序
 * 处理用户反馈回来的请求明细，入库
 * 针对用户反馈是失败请求，进行分析上报预警
 * 给HttpDns服务端智能分配A记录提供数据依据
 */
public class ScoreManager implements IScore {

    /**
     * 是否开启排序开关
     */
    public static boolean IS_SORT = true;

    private PlugInManager plugInManager = new PlugInManager();

    /**
     * 通过排序插件计算ip得分
     */
    @Override
    public String[] serverIpScore(DomainModel domainModel) {

        String[] IpArr = null;

        // 缓存中得到数据，进行排序，当ipmodelSize 大于1个的时候在参与排序
        if (domainModel.ipModelArr.size() > 1) {
            if (IS_SORT) {
                plugInManager.run(domainModel.ipModelArr);
            } else {
                Tools.randomSort(domainModel.ipModelArr);
            }
        }

        //转换数据格式
        IpArr = ListToArr(domainModel.ipModelArr);

        return IpArr;
    }

    @Override
    public String[] ListToArr(ArrayList<IpModel> list) {
        if (list == null || list.size() == 0) return null;
        String[] IpArr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) continue;
            IpArr[i] = list.get(i).ip;
        }
        return IpArr;
    }

    @Override
    public int[] ListToInt(ArrayList<IpModel> list) {
        if (list == null || list.size() == 0) return null;
        int[] sourceArr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) continue;
            sourceArr[i] = list.get(i).source;
        }
        return sourceArr;
    }
}
