package com.zxb.eshop.storm.bolt;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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

    private LRUMap<Long, Long> productCountMap = new LRUMap<>(1000); //storm api自带的LRUMap

    private static final Long GET_TOP3_TIME_INTERVAL = 10000L; //统计访问次数top3商品时间间隔

    private static final String TASKID_LIST_LOCK_PATH = "/taskid-list-lock";

    private static final String TASKID_LIST_PATH = "/taskid-list";

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.zkSession = ZooKeeperSession.getInstance();
        this.taskId = context.getThisTaskId();

        new Thread(new ProductCountThread()).start();

        //1. 初始化时将taskId写入一个zk node中，形成一个taskId列表
        //2. 然后将当前task对应统计出来的top3写入自己的taskid对应的zk node中
        //3. 并行的预热程序，根据1可以拿到所有的taskId列表
        //4. 然后并行预热程序根据每个taskid去获取锁，再从taski都对应的zk node中拿到最热商品信息并预热；
        // 4这里加锁的目的是防止多个预热程序重复预热同一个taskid对应的热门商品，保证只有一个预热线程去预热某个taskid统计出来的热门商品。
        //至此，topn热点商品的storm开发结束，数据全部写在zk node上了，自己写另外的程序定时去zk上拿并预热即可
        initTaskId(taskId);
    }

    private void initTaskId(int taskId){
        //先获取锁再设置，保证顺序, taskIds形如111,222,333
        zkSession.aquireDistributedLock(TASKID_LIST_LOCK_PATH);
        //判断node "/taskid-list"是否存在，不存在则需要创建
        zkSession.createNode(TASKID_LIST_PATH);
        String taskIds = zkSession.getNodeData(TASKID_LIST_PATH);
        StringBuilder sb = new StringBuilder(taskIds);
        if(StringUtils.isBlank(sb.toString())){
            //说明是第一个拿到锁的task
            sb.append(taskId);
        } else {
            sb.append(","+taskId);
        }
        logger.info("当前taskIds为：{}", sb.toString());
        zkSession.setNodeData(TASKID_LIST_PATH, sb.toString());

        zkSession.releaseDistributedLock(TASKID_LIST_LOCK_PATH);
    }

    private class ProductCountThread implements Runnable {

        @Override
        public void run() {
            List<Long> top3ProductList = new ArrayList<>();
            while (true) {
                Utils.sleep(GET_TOP3_TIME_INTERVAL);
                logger.info("productCountBolt计算top3 start--, productCountMap={}",productCountMap);
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
                zkSession.setNodeData("/task-hot-product-list-"+taskId, top3ProductStr);
                logger.info("productCountBolt计算top3 end--, top3ProductList={}",top3ProductList);

                //todo xuery 这里可能需要将"/taskid-status-"+id先清除下，不然路由到当前taskId的热门商品只能在第一次循环中成功预热，之后如果有其他商品进入这里
                //但是由于"/taskid-status-"+id已经为success了，并行预热线程是不会去预热的
//                zkSession.deleteZkNode("/taskid-status-" + taskId); //注释和打开看下效果
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
