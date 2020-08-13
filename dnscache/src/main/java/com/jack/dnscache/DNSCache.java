package com.jack.dnscache;

import android.content.Context;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.jack.dnscache.cache.DnsCacheManager;
import com.jack.dnscache.cache.IDnsCache;
import com.jack.dnscache.dnsp.DnsManager;
import com.jack.dnscache.dnsp.IDns;
import com.jack.dnscache.log.HttpDnsLogManager;
import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.HttpDnsPack;
import com.jack.dnscache.model.IpModel;
import com.jack.dnscache.net.ApacheHttpClientNetworkRequests;
import com.jack.dnscache.net.networktype.Constants;
import com.jack.dnscache.net.networktype.NetworkManager;
import com.jack.dnscache.net.networktype.NetworkStateReceiver;
import com.jack.dnscache.query.IQuery;
import com.jack.dnscache.query.QueryManager;
import com.jack.dnscache.score.IScore;
import com.jack.dnscache.score.ScoreManager;
import com.jack.dnscache.speedtest.ISpeedtest;
import com.jack.dnscache.speedtest.SpeedtestManager;
import com.jack.dnscache.thread.RealTimeThreadPool;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Lib库全局 对外实例对象
 *
 * @version V1.0
 */
public class DNSCache {

    /**
     * 是否开启该库
     */
    public static boolean isEnable = true;
    /**
     * 定时器任务轮询间隔
     */
    public static int timer_interval = 60 * 1000;
    private static DNSCache Instance = null;
    private static Context sContext;
    private static Object lock = new Object();
    /**
     * 缓存管理
     */
    public IDnsCache dnsCacheManager = null;
    /**
     * 数据查找
     */
    public IQuery queryManager = null;
    /**
     * ip质量排序（历史错误、服务器推荐优先级、本次测速、历史成功、最好成功时间）
     */
    public IScore scoreManager = null;
    /**
     * dns解析管理（httpdns、udpdns、poddns、localdns）
     */
    public IDns dnsManager = null;
    /**
     * 专为本次测速提供数据来源
     */
    public ISpeedtest speedtestManager = null;

    public DNSCache(Context ctx) {
        dnsCacheManager = new DnsCacheManager(ctx);
        queryManager = new QueryManager(dnsCacheManager);
        scoreManager = new ScoreManager();
        dnsManager = new DnsManager();
        speedtestManager = new SpeedtestManager();
        startTimer();
    }

    public static DNSCache getInstance() {
        if (null == Instance) {
            synchronized (lock) {
                if (Instance == null) {
                    Instance = new DNSCache(sContext);
                }
            }
        }
        return Instance;
    }

    /**
     * 初始化建议放在Application中处理
     */
    public static void Init(Context ctx) {
        if (null == ctx) {
            throw new RuntimeException("DNSCache Init; context can not be null!!!");
        }
        sContext = ctx.getApplicationContext();
        // 根据配置文件 初始化策略
        DNSCacheConfig.InitCfg(sContext);
        NetworkManager.CreateInstance(sContext);
        AppConfigUtil.init(sContext);
        NetworkStateReceiver.register(sContext);
        Instance = null;
    }

    /**
     * 预加载逻辑（可选） 建议尽早调用
     * 可提前预加载需要解析的域名，并将解析数据缓存到内存中。
     */
    public void preLoadDomains(final String[] domains) {
        for (String domain : domains) {
            checkUpdates(domain, true);
        }
    }

    public IDnsCache getDnsCacheManager() {
        return dnsCacheManager;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取 HttpDNS信息
     *
     * @param url 传入的Url
     * @return 返回排序后的可直接使用接口
     */
    public DomainInfo[] getDomainServerIp(String url) {
        String host = Tools.getHostName(url);
        if (isEnable) {
            if (!TextUtils.isEmpty(host) && Tools.isIPV4(host)) {
                DomainInfo[] info = new DomainInfo[1];
                info[0] = new DomainInfo("", url, "");
                return info;
            }
            // 查询domain对应的server ip数组
            final DomainModel domainModel = queryManager.queryDomainIp(String.valueOf(NetworkManager.getInstance().getSPID()), host);


            // 如果本地cache 和 内置数据都没有 返回null，然后马上查询数据
            if (null == domainModel || domainModel.id == -1) {
                this.checkUpdates(host, true);
                if (null == domainModel) {
                    return null;
                }
            }

            HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN, domainModel.tojson(), true);

            ArrayList<IpModel> result = filterInvalidIp(domainModel.ipModelArr);
            String[] scoreIpArray = scoreManager.ListToArr(result);

            if (scoreIpArray == null || scoreIpArray.length == 0) {
                return null; // 排序错误 终端后续流程
            }

            // 转换成需要返回的数据模型
            DomainInfo[] domainInfoList = DomainInfo.DomainInfoFactory(scoreIpArray, url, host);

            return domainInfoList;
        } else {
            return null;
        }
    }

    /**
     * 过滤无效ip数据
     *
     * @param ipModelArr
     * @return
     */
    private ArrayList<IpModel> filterInvalidIp(ArrayList<IpModel> ipModelArr) {
        ArrayList<IpModel> result = new ArrayList<IpModel>();
        for (IpModel ipModel : ipModelArr) {
            if (!("" + SpeedtestManager.MAX_OVERTIME_RTT).equals(ipModel.rtt)) {
                result.add(ipModel);
            }
        }
        return result;
    }

    private boolean isSupport(String host) {
        return (DNSCacheConfig.domainSupportList.size() == 0) || (DNSCacheConfig.domainSupportList.contains(host));
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    private ConcurrentHashMap<String, UpdateTask> mRunningTasks = new ConcurrentHashMap<String, UpdateTask>();

    /**
     * 从httpdns 服务器重新拉取数据
     */
    private void checkUpdates(String domain, boolean speedTest) {
        if (isSupport(domain)) {
            final String host = domain;
            final boolean needSpeedTest = speedTest;
            UpdateTask task = mRunningTasks.get(host);
            if (null == task) {
                UpdateTask updateTask = new UpdateTask(new Runnable() {
                    @Override
                    public void run() {
                        Thread.currentThread().setName("Get Http Dns Data");
                        getHttpDnsData(host);
                        mRunningTasks.remove(host);
                        if (needSpeedTest) {
                            RealTimeThreadPool.getInstance().execute(new SpeedTestTask());
                        }
                    }
                });
                mRunningTasks.put(host, updateTask);
                updateTask.start();
            } else {
                long beginTime = task.getBeginTime();
                long now = System.currentTimeMillis();
                // 上次拉取超时，这次再开一个线程继续
                if (now - beginTime > 30 * 1000) {
                    task.start();
                }
            }
        }
    }

    static class UpdateTask {

        public Runnable runnable;

        public long beginTime;

        public UpdateTask(Runnable runnable) {
            super();
            this.runnable = runnable;
            this.beginTime = System.currentTimeMillis();
        }

        public void start() {
            Thread thread = new Thread(runnable);
            thread.start();
        }

        public long getBeginTime() {
            return beginTime;
        }
    }

    /**
     * 根据 host 更新数据
     *
     * @param host
     */
    private final DomainModel getHttpDnsData(String host) {

        // 获取 httpdns 数据
        HttpDnsPack httpDnsPack = dnsManager.requestDns(host);

        if (httpDnsPack == null) {
            return null; // 没有从htppdns服务器获取正确的数据。必须中断下面流程
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN, httpDnsPack.toJson(),
                true);
        // 插入本地 cache
        DomainModel domainModel = dnsCacheManager.insertDnsCache(httpDnsPack);

        return domainModel;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 定时器休眠时间
     */
    public final int sleepTime = timer_interval;

    /**
     * 启动定时器
     */
    private void startTimer() {
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(task, 0, sleepTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 定时器Obj
     */
    private ScheduledExecutorService executorService;

    /**
     * TimerTask 运行时间
     */
    public long TimerTaskOldRunTime = 0;
    /**
     * 上次测速时间
     */
    private long lastSpeedTime;
    /**
     * 上次日志上传时间
     */
    private long lastLogTime;

    /**
     * 定时器还多久启动
     */
    public long getTimerDelayedStartTime() {
        return (sleepTime - (System.currentTimeMillis() - TimerTaskOldRunTime)) / 1000;
    }

    /**
     * 定时器任务
     */
    private Runnable task = new Runnable() {

        @Override
        public void run() {
            TimerTaskOldRunTime = System.currentTimeMillis();
            //无网络情况下不执行任何后台任务操作
            if (NetworkManager.Util.getNetworkType() == Constants.NETWORK_TYPE_UNCONNECTED || NetworkManager.Util.getNetworkType() == Constants.MOBILE_UNKNOWN) {
                return;
            }
            /************************* 更新过期数据 ********************************/
            Thread.currentThread().setName("HTTP DNS TimerTask");
            final ArrayList<DomainModel> list = dnsCacheManager.getExpireDnsCache();
            for (DomainModel model : list) {
                checkUpdates(model.domain, false);
            }

            long now = System.currentTimeMillis();
            /************************* 测速逻辑 ********************************/
            if (now - lastSpeedTime > SpeedtestManager.time_interval - 3) {
                lastSpeedTime = now;
                RealTimeThreadPool.getInstance().execute(new SpeedTestTask());
            }

            /************************* 日志上报相关 ********************************/
            now = System.currentTimeMillis();
            if (HttpDnsLogManager.LOG_UPLOAD_SWITCH && now - lastLogTime > HttpDnsLogManager.time_interval) {
                lastLogTime = now;
                // 判断当前是wifi网络才能上传
                if (NetworkManager.Util.getNetworkType() == Constants.NETWORK_TYPE_WIFI) {
                    RealTimeThreadPool.getInstance().execute(new LogUpLoadTask());
                }
            }
        }
    };

    /**
     * 测速模块
     */
    private class SpeedTestTask implements Runnable {

        @Override
        public void run() {
            ArrayList<DomainModel> list = dnsCacheManager.getAllMemoryCache();
            updateSpeedInfo(list);
        }

        private void updateSpeedInfo(ArrayList<DomainModel> list) {
            for (DomainModel domainModel : list) {
                ArrayList<IpModel> ipArray = domainModel.ipModelArr;
                if (ipArray == null || ipArray.size() < 1) {
                    continue;
                }
                for (IpModel ipModel : ipArray) {
                    int rtt = speedtestManager.speedTest(ipModel.ip, domainModel.domain);
                    boolean succ = rtt > SpeedtestManager.OCUR_ERROR;
                    if (succ) {
                        ipModel.rtt = String.valueOf(rtt);
                        ipModel.success_num = String.valueOf((Integer.valueOf(ipModel.success_num) + 1));
                        ipModel.finally_success_time = String.valueOf(System.currentTimeMillis());
                    } else {
                        ipModel.rtt = String.valueOf(SpeedtestManager.MAX_OVERTIME_RTT);
                        ipModel.err_num = String.valueOf((Integer.valueOf(ipModel.err_num) + 1));
                        ipModel.finally_fail_time = String.valueOf(System.currentTimeMillis());
                    }
                }
                scoreManager.serverIpScore(domainModel);
                dnsCacheManager.setSpeedInfo(ipArray);
            }
        }
    }

    /**
     * 日志上报
     */
    private class LogUpLoadTask implements Runnable {
        @Override
        public void run() {
            File logFile = HttpDnsLogManager.getInstance().getLogFile();
            if (null == logFile || !logFile.exists()) {
                return;
            }
            boolean succ = false;
            // upload
            try {
                succ = ApacheHttpClientNetworkRequests.upLoadFile(HttpDnsLogManager.LOG_UPLOAD_API, logFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // succ
            if (succ) {
                logFile.delete();
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    /**
     * 网络环境发生变化 刷新缓存数据 暂时先不需要 预处理逻辑。 用户直接请求的时候会更新数据。 （会有一次走本地dns ，
     * 后期优化这个方法，主动请求缓存的数据）
     *
     * @param networkInfo
     */
    public void onNetworkStatusChanged(NetworkInfo networkInfo) {
        if (null != dnsCacheManager) {
            dnsCacheManager.clearMemoryCache();
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////

}
