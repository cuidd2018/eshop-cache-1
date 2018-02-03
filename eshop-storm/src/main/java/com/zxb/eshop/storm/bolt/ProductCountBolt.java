package com.zxb.eshop.storm.bolt;

import com.alibaba.fastjson.JSONArray;
import com.zxb.eshop.storm.utils.MyHttpUtil;
import com.zxb.eshop.storm.vo.MyHttpResponse;
import com.zxb.eshop.storm.zk.ZooKeeperSession;
import org.apache.storm.shade.org.apache.commons.lang.StringUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 对productId做次数统计
 * Created by xuery on 2018/1/31.
 */
public class ProductCountBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(ProductCountBolt.class);

    private OutputCollector collector;

    private ZooKeeperSession zkSession;

    private int taskId; //bolt中运行代码的task对应的id

    //storm api自带的LRUMap,这个大小也是需要根据实际场景调整的
    //todo 实际应用场景中一般不会采用类内部变量保存数据的方法（保存在内存中），因为当topology被不小心死掉或者被杀掉，重启topology之后，所有数据都丢失了，一般是需要采用kv框架持久化的
    private LRUMap<Long, Long> productCountMap = new LRUMap<>(1000);

    private static final Long GET_TOP3_TIME_INTERVAL = 20000L; //统计访问次数top3商品时间间隔

    private static final Long GET_HOT_PRODUCT_INTERVAL = 10000L; //获取热点商品的时间间隔

    private static final int N = 10; //热点商品是普通商品访问次数的倍数阈值

    private static final String TASKID_LIST_LOCK_PATH = "/taskid-list-lock";

    private static final String TASKID_LIST_PATH = "/taskid-list";

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.zkSession = ZooKeeperSession.getInstance();
        this.taskId = context.getThisTaskId();

//        new Thread(new ProductCountThread()).start();

        new Thread(new HotProductFindThread()).start();

        //1. 初始化时将taskId写入一个zk node中，形成一个taskId列表
        //2. 然后将当前task对应统计出来的top3写入自己的taskid对应的zk node中
        //3. 并行的预热程序，根据1可以拿到所有的taskId列表
        //4. 然后并行预热程序根据每个taskid去获取锁，再从taski都对应的zk node中拿到最热商品信息并预热；
        // 4这里加锁的目的是防止多个预热程序重复预热同一个taskid对应的热门商品，保证只有一个预热线程去预热某个taskid统计出来的热门商品。
        //至此，topn热点商品的storm开发结束，数据全部写在zk node上了，自己写另外的程序定时去zk上拿并预热即可
        initTaskId(taskId);
    }

    private void initTaskId(int taskId) {
        //先获取锁再设置，保证顺序, taskIds形如111,222,333
        zkSession.aquireDistributedLock(TASKID_LIST_LOCK_PATH);
        //判断node "/taskid-list"是否存在，不存在则需要创建
        zkSession.createNode(TASKID_LIST_PATH);
        String taskIds = zkSession.getNodeData(TASKID_LIST_PATH);
        StringBuilder sb = new StringBuilder(taskIds);
        if (StringUtils.isBlank(sb.toString())) {
            //说明是第一个拿到锁的task
            sb.append(taskId);
        } else {
            sb.append("," + taskId);
        }
        logger.info("当前taskIds为：{}", sb.toString());
        zkSession.setNodeData(TASKID_LIST_PATH, sb.toString());

        zkSession.releaseDistributedLock(TASKID_LIST_LOCK_PATH);
    }

    /**
     * 统计热点商品/秒杀商品，与热商品还是有差别的，这里主要关注【某个商品的短时间内访问量】与【后95%所有商品访问量的平均值】，大于平均值比如10倍
     * 则订为热点商品
     */
    private class HotProductFindThread implements Runnable {
        @Override
        public void run() {
            List<Long> hotProductList = new ArrayList<>();
            List<Long> lastTimeHotProductList = new ArrayList<>();
            while (true) {
                //计算规则，先全局排序
                //计算出后95%商品的平均访问量averCnt
                //从最大值开始遍历，当值大于N*averCnt，则为热点商品
                //将热点商品id http推送到分发层nginx，将热点商品id和热点商品详情http推送到所有的应用层nginx
                //推送之前要拿到之前的推送热点列表，并对比，没有了的说明可能是非热点商品了需要清除nginx缓存，新加的则是新的热点商品需要设置缓存；所以需要保存一份之前的推送列表

                Utils.sleep(GET_HOT_PRODUCT_INTERVAL);
                logger.info("HotProductFindThread获取热点商品start--, productCountMap={}", productCountMap);
                hotProductList.clear();

                //1.先按照从大到小排序
                Set<Map.Entry<Long, Long>> entrySet = productCountMap.entrySet();
                List<Map.Entry<Long, Long>> listEntry = new LinkedList<>(entrySet);
                Collections.sort(listEntry, new Comparator<Map.Entry<Long, Long>>() {
                    @Override
                    public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                        Long value1 = o1.getValue() == null ? 0L : o1.getValue();
                        Long value2 = o2.getValue() == null ? 0L : o2.getValue();
                        return value2.compareTo(value1); //倒排
                    }
                });
                if (listEntry.size() <= 1) {
                    continue;
                }

                //2. 算出后95%访问次数平均值, 不用纠结size很小的时候，size很小的时候尽量让更多的商品成为热点商品就可以了
                int calculateCnt = (int) Math.floor(listEntry.size() * 0.95); //向下取整
                Long sumCnt = 0L;
                for (int i = listEntry.size() - 1; i >= listEntry.size() - calculateCnt; i--) {
                    sumCnt += listEntry.get(i).getValue();
                }
                Long averCnt = sumCnt / calculateCnt;

                //3. 统计当前的热点商品并判断之前是否发送过，为发送则需要发送nginx
                try {
                    for (int i = 0; i < listEntry.size(); i++) {
                        Map.Entry<Long, Long> entry = listEntry.get(i);
                        if (entry.getValue().compareTo(N * averCnt) >= 0) {
                            logger.info("发现一个热点商品，productId={}", entry.getKey());
                            hotProductList.add(entry.getKey());

                            //上次未发送过，说明是新的热点商品需要发送nginx
                            if (!lastTimeHotProductList.contains(entry.getKey())) {
                                sendNewHotProduct(entry);
                            }
                        } else {
                            //降序，所以后面的不可能满足了
                            break;
                        }
                    }
                    //4. 实时感知热数据的消失并发送请求取消nginx分发层的降级策略
                    if (!lastTimeHotProductList.isEmpty()) {
                        sendCancelHotProduct(hotProductList, lastTimeHotProductList);
                    }
                    lastTimeHotProductList.clear();
                    lastTimeHotProductList.addAll(hotProductList);
                } catch (Exception e) {
                    logger.error("统计热点商品出错，error:{}", e);
                }
            }
        }

        /**
         * 取消已经是非热点数据的nginx应用层的降级策略
         *
         * @param hotProductList
         * @param lastTimeHotProductList
         */
        private void sendCancelHotProduct(List<Long> hotProductList, List<Long> lastTimeHotProductList) {
            //需要对比去除已经是非热点的数据
            for (int i = 0; i < lastTimeHotProductList.size(); i++) {
                if (!hotProductList.contains(lastTimeHotProductList.get(i))) {
                    String cancelUrl = "http://192.168.95.138/cancel_hot";
                    Map<String, String> map = new HashMap<>();
                    map.put("productId", lastTimeHotProductList.get(i).toString());
                    logger.info("当前商品已经是非热点商品，发送取消请求到分发层nginx,productId={}", map.get("productId"));
                    MyHttpUtil.doHttpGet(cancelUrl, map);
                }
            }
        }

        /**
         * 发送新热点数据到nginx
         *
         * @param entry
         */
        private void sendNewHotProduct(Map.Entry<Long, Long> entry) {
            //发送新热点商品id到nginx分发层
            String distributeNginxUrl = "http://192.168.95.138/hot";
            Map<String, String> distriMap = new HashMap<>();
            distriMap.put("productId", entry.getKey().toString());
            MyHttpUtil.doHttpGet(distributeNginxUrl, distriMap);

            //发送新热点商品id及详情到nginx应用层
            String cacheServiceUrl = "http://10.118.52.120:8080/getProductInfo"; //这个需要先测试下
            Map<String, String> cacheMap = new HashMap<>();
            cacheMap.put("productId", entry.getKey().toString());
            MyHttpResponse response = MyHttpUtil.doHttpGet(cacheServiceUrl, cacheMap);

            logger.info("热点商品发送http请求获取商品详情结果：{}", response);
            if (response != null) {
                String productInfoStr = response.getResponseBody();
                cacheMap.put("productInfo", productInfoStr);
            }
            String[] appNginxUrls = new String[]{
                    "http://192.168.95.135/hot",
                    "http://192.168.95.137/hot"
            };
            for (String appNginxUrl : appNginxUrls) {
                MyHttpUtil.doHttpGet(appNginxUrl, cacheMap);
            }
        }
    }

    /**
     * 统计topn热商品信息
     */
    private class ProductCountThread implements Runnable {

        @Override
        public void run() {
            List<Long> top3ProductList = new ArrayList<>();
            while (true) {
                Utils.sleep(GET_TOP3_TIME_INTERVAL);
                logger.info("productCountBolt计算top3 start--, productCountMap={}", productCountMap);
                top3ProductList.clear();
                //直接复制一份LRUMap中一份entrySet,对values排下序取前三个即可
                Set<Map.Entry<Long, Long>> entrySet = productCountMap.entrySet();
                List<Map.Entry<Long, Long>> listEntry = new LinkedList<>(entrySet);
                Collections.sort(listEntry, new Comparator<Map.Entry<Long, Long>>() {
                    @Override
                    public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                        Long value1 = o1.getValue() == null ? 0L : o1.getValue();
                        Long value2 = o2.getValue() == null ? 0L : o2.getValue();
                        return value2.compareTo(value1); //倒排
                    }
                });
                if (listEntry.size() == 0) {
                    continue;
                }
                int sz = listEntry.size() > 3 ? 3 : listEntry.size(); //listEntry元素大于3则取前三个，否则去listEntry.size()个
                for (int i = 0; i < sz; i++) {
                    top3ProductList.add(listEntry.get(i).getKey());
                }
                //将统计出来的top3写入zk对应的node中, 是当前task自己统计出的top3所以不用加锁
                String top3ProductStr = JSONArray.toJSONString(top3ProductList);
                zkSession.createNode("/task-hot-product-list-" + taskId);
                zkSession.setNodeData("/task-hot-product-list-" + taskId, top3ProductStr);
                logger.info("productCountBolt计算top3 end--, top3ProductList={}", top3ProductList);

                /**
                 * 这里必须将"/taskid-status-"+id先清除下，不然路由到当前taskId的热门商品只能在第一次循环中成功预热，之后如果有其他商品进入这里
                 *但是由于"/taskid-status-"+id已经为success了，并行预热线程是不会去预热的
                 * 不加的话只能预热一次，这里循环去统计热点数据也就没意义了
                 */
                zkSession.deleteZkNode("/taskid-status-" + taskId); //注释和打开看下效果
            }
        }
    }

    @Override
    public void execute(Tuple tuple) {
        Long productId = tuple.getLongByField("productId");
        logger.info(">>>ProductCountBolt接收到上一级发射的商品id为：{},并统计次数，taskId={}", productId, taskId);
        Long count = productCountMap.get(productId);
        if (count == null) {
            productCountMap.put(productId, 1L);
        } else {
            productCountMap.put(productId, count + 1);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
