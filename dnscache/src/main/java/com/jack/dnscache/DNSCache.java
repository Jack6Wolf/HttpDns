package com.jack.dnscache;

import android.content.Context;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.jack.dnscache.cache.DnsCacheManager;
import com.jack.dnscache.cache.IDnsCache;
import com.jack.dnscache.dnsp.DnsManager;
import com.jack.dnscache.dnsp.IDns;
import com.jack.dnscache.dnsp.IDnsProvider;
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
import java.util.Iterator;
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
    public static int timer_interval = 180 * 1000;
    private static DNSCache Instance = null;
    private static Context sContext;
    private static Object lock = new Object();
    /**
     * 定时器休眠时间
     */
    public final int sleepTime = timer_interval;
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
    /**
     * TimerTask 运行时间
     */
    public long TimerTaskOldRunTime = 0;

    /**
     * 正在更新的domain任务集合
     */
    private ConcurrentHashMap<String, UpdateTask> mRunningTasks = new ConcurrentHashMap<String, UpdateTask>();
    /**
     * 定时器Obj
     */
    private ScheduledExecutorService executorService;
    /**
     * 上次测速时间
     */
    private long lastSpeedTime;

    // ///////////////////////////////////////////////////////////////////////////////////
    /**
     * 上次日志上传时间
     */
    private long lastLogTime;
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
            //取出过期domain数据
            final ArrayList<DomainModel> list = dnsCacheManager.getExpireDnsCache();
            for (DomainModel model : list) {
                //更新
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

    public DNSCache(Context ctx) {
        dnsCacheManager = new DnsCacheManager(ctx);
        queryManager = new QueryManager(dnsCacheManager);
        scoreManager = new ScoreManager();
        dnsManager = new DnsManager();
        speedtestManager = new SpeedtestManager();
        //开启60s轮询任务
        startTimer();
    }

    // ///////////////////////////////////////////////////////////////////////////////////

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
     * 初始化
     * 建议放在Application中处理
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
     * 获取 HttpDNS信息,异步解析
     * 若接口返回null，为避免影响业务请降级到local dns解析策略。
     *
     * @param url 传入的Url
     * @return 返回排序后的可直接使用接口
     */
    public DomainInfo[] getDomainServerIp(String url) {
        String host = Tools.getHostName(url);
        if (isEnable) {
            //如果直接是ip的话直接处理返回
            if (!TextUtils.isEmpty(host) && Tools.isIPV4(host)) {
                DomainInfo[] info = new DomainInfo[1];
                info[0] = new DomainInfo("", host, url, "", IDnsProvider.ORIGINAL);
                return info;
            }
            // 根据sp 查询domain对应的server ip数组
            DomainModel domainModel = queryManager.queryDomainIp(String.valueOf(NetworkManager.getInstance().getSPID()), host);

            // 如果本地cache 和 内置数据都没有 返回null，然后马上查询数据
            if (null == domainModel || domainModel.id == -1) {
                checkUpdates(host, true);
                if (null == domainModel) {
                    return null;
                }
            }

            HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN, domainModel.tojson(), true);
            //过滤一下无效ip
            ArrayList<IpModel> result = filterInvalidIp(domainModel.ipModelArr);
            String[] scoreIpArray = scoreManager.ListToArr(result);
            int[] sourceIpArray = scoreManager.ListToInt(result);

            if (scoreIpArray == null || scoreIpArray.length == 0) {
                return null; // 转换错误 终端后续流程
            }

            // 转换成需要返回的数据模型
            DomainInfo[] domainInfoList = DomainInfo.DomainInfoFactory(scoreIpArray, url, host, sourceIpArray);

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

    /**
     * 是否在白名单里面
     */
    private boolean isSupport(String host) {
        return (DNSCacheConfig.domainSupportList.size() == 0) || (DNSCacheConfig.domainSupportList.contains(host));
    }

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
                        //移除任务
                        mRunningTasks.remove(host);
                        if (needSpeedTest) {
                            //测速模块
                            RealTimeThreadPool.getInstance().execute(new SpeedTestTask());
                        }
                    }
                });
                mRunningTasks.put(host, updateTask);
                //开启新线程获取httpdns
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

    /**
     * 根据host从server获取dns数据
     */
    private final DomainModel getHttpDnsData(String host) {

        // 从server获取最新dns数据
        HttpDnsPack httpDnsPack = dnsManager.requestDns(host);

        if (httpDnsPack == null) {
            return null; // 没有从dns服务器获取正确的数据。必须中断下面流程
        }

        HttpDnsLogManager.getInstance().writeLog(HttpDnsLogManager.TYPE_INFO, HttpDnsLogManager.ACTION_INFO_DOMAIN, httpDnsPack.toJson(),
                true);
        // 插入本地cache和数据库
        DomainModel domainModel = dnsCacheManager.insertDnsCache(httpDnsPack);

        return domainModel;
    }

    /**
     * 启动定时器
     */
    private void startTimer() {
        executorService = new ScheduledThreadPoolExecutor(1);
        executorService.scheduleAtFixedRate(task, 0, sleepTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 定时器还多久启动
     */
    public long getTimerDelayedStartTime() {
        return (sleepTime - (System.currentTimeMillis() - TimerTaskOldRunTime)) / 1000;
    }

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

    /**
     * 更新任务
     */
    private static class UpdateTask {

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
     * 测速模块，供ip排序模块使用
     */
    private class SpeedTestTask implements Runnable {

        @Override
        public void run() {
            //获取缓存中全部的 DomainModel
            ArrayList<DomainModel> list = dnsCacheManager.getAllMemoryCache();
            updateSpeedInfo(list);
        }

        private void updateSpeedInfo(ArrayList<DomainModel> list) {
            for (DomainModel domainModel : list) {
                ArrayList<IpModel> ipArray = domainModel.ipModelArr;
                if (ipArray == null || ipArray.size() < 1) {
                    //单个ip不用排序了
                    continue;
                }
                //使用迭代器避免出现ConcurrentModificationException
                Iterator<IpModel> iterator = ipArray.iterator();
                while (iterator.hasNext()) {
                    IpModel ipModel = iterator.next();
                    //根据测速模块的
                    int rtt = speedtestManager.speedTest(ipModel.ip, domainModel.domain);
                    boolean succ = rtt > SpeedtestManager.OCUR_ERROR;
                    if (succ) {
                        ipModel.rtt = String.valueOf(rtt);
                        //算作链接成功一次
                        ipModel.success_num = String.valueOf((Integer.parseInt(ipModel.success_num) + 1));
                        ipModel.finally_success_time = String.valueOf(System.currentTimeMillis());
                    } else {
                        ipModel.rtt = String.valueOf(SpeedtestManager.MAX_OVERTIME_RTT);
                        //算作链接失败一次
                        ipModel.err_num = String.valueOf((Integer.parseInt(ipModel.err_num) + 1));
                        ipModel.finally_fail_time = String.valueOf(System.currentTimeMillis());
                    }
                }
                //交给排序模块重新排序
                scoreManager.serverIpScore(domainModel);
                //只更新数据库缓存模块ip的顺序
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


}
