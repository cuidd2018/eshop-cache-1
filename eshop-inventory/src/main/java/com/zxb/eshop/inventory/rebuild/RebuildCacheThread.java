package com.zxb.eshop.inventory.rebuild;

import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.service.CacheService;
import com.zxb.eshop.inventory.spring.SpringContext;
import com.zxb.eshop.inventory.zk.ZooKeeperSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 更新缓存线程，利用内存队列异步完成
 * 类比库存更新
 * Created by xuery on 2018/1/29.
 */
public class RebuildCacheThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RebuildCacheThread.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CacheService cacheService;

    @Override
    public void run() {
        this.cacheService = (CacheService) SpringContext.getApplicationContext().getBean("cacheService");
        RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();

        while (true) {
            ProductInfo productInfo = rebuildCacheQueue.takeProductInfo();
            //加分布式锁
            zooKeeperSession.aquireDistributedLock(productInfo.getId());
            logger.info("前端更新商品信息获取锁成功，productInfo:{}",productInfo);
            //获取到锁，开始更新
            ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productInfo.getId());
            if (existedProductInfo != null) {
                try {
                    Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());
                    Date date = sdf.parse(productInfo.getModifiedTime());
                    //时间不是最新的则不更新
                    if (date.before(existedDate)) {
                        logger.info("current date:{} is before existed date:{}",productInfo.getModifiedTime(), existedProductInfo.getModifiedTime());
                        continue;
                    }
                } catch (Exception e) {
                    logger.error("日期转换比较失败，error:", e);
                }
            }
            //开始更新
            cacheService.saveProductInfo2LocalCache(productInfo);
            cacheService.saveProductInfo2RedisCache(productInfo);
            logger.info("前端更新商品信息redis缓存成功，productInfo:{}",productInfo);
            zooKeeperSession.releaseDistributedLock(productInfo.getId());
            logger.info("前端更新商品信息，释放锁，productInfo:{}",cacheService.getProductInfoFromRedisCache(productInfo.getId()));
        }

    }
}
