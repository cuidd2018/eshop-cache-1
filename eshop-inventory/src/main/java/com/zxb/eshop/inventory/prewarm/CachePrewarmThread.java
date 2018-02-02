package com.zxb.eshop.inventory.prewarm;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.service.CacheService;
import com.zxb.eshop.inventory.spring.SpringContext;
import com.zxb.eshop.inventory.zk.ZooKeeperSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 缓存预热线程
 * Created by xuery on 2018/2/1.
 */
public class CachePrewarmThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CachePrewarmThread.class);

    private static final String TASKID_LIST_PATH = "/taskid-list";

    @Override
    public void run() {

        CacheService cacheService = (CacheService) SpringContext.getApplicationContext().getBean("cacheService");

        //先获取taskIds
        ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
        //需不需要加锁，防止重复读取，细化到每个taskId加锁
        String taskIds = zkSession.getNodeData(TASKID_LIST_PATH);
        logger.info(">>>CachePrewarmThread start--,taskIds={}, threadInfo={}", taskIds, Thread.currentThread());
        if (StringUtils.isNotBlank(taskIds)) {
            String[] taskIdStr = taskIds.split(",");
            for (String taskId : taskIdStr) {
                String taskidLockPath = "/taskid-lock-" + taskId;
                boolean result = zkSession.aquireFastFailDistributedLock(taskidLockPath);
                logger.info(">>>get taskid={} lock, result={}, threadInfo={}", taskId, result, Thread.currentThread());
                //说明有其他线程预热当前taskid对应的热商品信息，则不管了
                if (!result) {
                    continue;
                }
                //还需要判断下当前taskid对应的商品是否已经被预热过了
//                zkSession.aquireDistributedLock("/taskid-status-lock-"+taskId); //这锁不用加
                String taskIdStatus = zkSession.getNodeData("/taskid-status-" + taskId);
                logger.info(">>>当前task对应的top3热门商品预热结果：{}, threadInfo={}", taskIdStatus, Thread.currentThread());
                //说明需要预热
                if (StringUtils.isBlank(taskIdStatus)) {
                    String hotProductStr = zkSession.getNodeData("/task-hot-product-list-" + taskId);
                    if (StringUtils.isNotBlank(hotProductStr)) {
                        JSONArray productIdJSONArray = JSONArray.parseArray(hotProductStr);

                        for (int i = 0; i < productIdJSONArray.size(); i++) {
                            Long productId = productIdJSONArray.getLong(i);
                            String productInfoJSON = "{\"id\": " + productId + ", \"name\": \"iphoneX手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:00:00\"}";
                            ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
                            cacheService.saveProductInfo2LocalCache(productInfo);
                            logger.info("热门商品【{}】缓存到本地:{}", productId, cacheService.getProductInfoFromLocalCache(productId));
                            cacheService.saveProductInfo2RedisCache(productInfo);
                            logger.info("热门商品【{}】缓存到redis:{}", productId, cacheService.getProductInfoFromRedisCache(productId));
                        }
                        logger.info(">>>当前task对应的top3热门商品预热完成, productIds={}, threadInfo={}", hotProductStr, Thread.currentThread());
                        //设置当前taskid的热门商品以及被预热完的标志
                        zkSession.createNode("/taskid-status-" + taskId);
                        zkSession.setNodeData("/taskid-status-" + taskId, "taskid=" + taskId + " is already prewarmed");
                    }
                }
                //预热或者不用预热完成
                zkSession.releaseDistributedLock(taskidLockPath);
                Utils.sleep(500); //todo 这里故意停止500ms，看看多线程预热时是否会重复预热，实际中是要去除的
            }
        }
    }
}
