package com.zxb.eshop.inventory.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.model.ShopInfo;
import com.zxb.eshop.inventory.service.CacheService;
import com.zxb.eshop.inventory.spring.SpringContext;
import com.zxb.eshop.inventory.utils.SleepUtil;
import com.zxb.eshop.inventory.zk.ZooKeeperSession;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xuery on 2018/1/26.
 */
public class KafkaMessageProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageProcessor.class);

    private KafkaStream kafkaStream;

    private CacheService cacheService;

    public KafkaMessageProcessor(KafkaStream kafkaStream) {
        this.kafkaStream = kafkaStream;
        this.cacheService = (CacheService) SpringContext.getApplicationContext().getBean("cacheService");
    }

    @Override
    public void run() {
        ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
        while (it.hasNext()) {
            try {
                String message = new String(it.next().message());
                logger.info("kafka消费信息为：{}", message);
                if (StringUtils.isBlank(message)) {
                    continue;
                }
                JSONObject messageJsonObject = JSON.parseObject(message);
                String serviceId = messageJsonObject.getString("serviceId");

                //判断类别
                logger.info("开始判断类别");
                if ("productInfoService".equals(serviceId)) {
                    processProductInfoChangeMessage(messageJsonObject);
                } else if ("shopInfoService".equals(serviceId)) {
                    processShopInfoChangeMessage(messageJsonObject);
                }
            } catch (Exception e) {
                logger.info("kafka消费者消费数据出错，error:", e);
            }
        }
    }

    /**
     * 处理商品信息变更的消息
     *
     * @param messageJsonObject
     */
    private void processProductInfoChangeMessage(JSONObject messageJsonObject) {
//        Long productId = messageJsonObject.getLong("productId");

        //这里直接模拟数据，实际中可以替换为实际的数据
        String productInfoJSON = "{\"id\": 6, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": " +
                "\"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\",\"shopId\": 1,\"modifiedTime\": \"2017-01-01 12:00:00\"}";

        ProductInfo productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
        Long productId = productInfo.getId();

        //这里加分布式锁
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getInstance();
        zooKeeperSession.aquireDistributedLock(productInfo.getId());
        logger.info("通过kafka更新商品信息，获取锁成功，productInfo:{}",productInfo);

        SleepUtil.pause(30*1000);//模拟，停止60s

        //获取到了锁还是要从redis中查询是否有缓存，并对比时间，【实际开发中应该不用，这里只是模拟】
        ProductInfo existedProductInfo = cacheService.getProductInfoFromRedisCache(productId);
        if(existedProductInfo != null){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date existedDate = sdf.parse(existedProductInfo.getModifiedTime());
                Date date = sdf.parse(productInfo.getModifiedTime());
                //如果是这种情况则说明当前不是最新的，不需要更新
                if(date.before(existedDate)){
                    logger.info("current date:{} is before existed date:{}",productInfo.getModifiedTime(), existedProductInfo.getModifiedTime());
                    return;
                }
                logger.info("productInfo>>>>"+productInfo+",cacheService:"+cacheService);
                cacheService.saveProductInfo2LocalCache(productInfo);
                logger.info(">>>获取刚保存到本地缓存的商品信息：" + cacheService.getProductInfoFromLocalCache(productInfo.getId()));
                cacheService.saveProductInfo2RedisCache(productInfo);
                logger.info("通过kafka更新商品信息成功，更新结果为：{}",cacheService.getProductInfoFromRedisCache(productInfo.getId()));
            } catch (Exception e){
                logger.error("日期转换失败，productId=",productInfo.getId());
            } finally {
                //更新完成后，释放锁; 教程中程序是有bug的，上面直接return不会释放锁，所以一定要再finally中释放锁
                zooKeeperSession.releaseDistributedLock(productId);
                logger.info("通过kafka更新商品信息，释放锁，productInfo:{}",productInfo);
            }
        }
    }


    /**
     * 处理店铺信息变更的消息
     *
     * @param messageJSONObject
     */
    private void processShopInfoChangeMessage(JSONObject messageJSONObject) {
        // 提取出店铺id
        Long shopId = messageJSONObject.getLong("shopId");

        // 调用商品信息服务的接口
        // 直接用注释模拟：getProductInfo?productId=1，传递过去
        // 商品信息服务，一般来说就会去查询数据库，去获取productId=1的商品信息，然后返回回来

        // 龙果有分布式事务的课程，主要讲解的分布式事务几种解决方案，里面也涉及到了一些mq，或者其他的一些技术，但是那些技术都是浅浅的给你搭建一下，使用
        // 你从一个课程里，还是学到的是里面围绕的讲解的一些核心的知识
        // 缓存架构：高并发、高性能、海量数据，等场景

        String shopInfoJSON = "{\"id\": 4, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";
        ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);
        cacheService.saveShopInfo2LocalCache(shopInfo);
        logger.info(">>>获取刚保存到本地缓存的店铺信息：" + cacheService.getShopInfoFromLocalCache(shopInfo.getId()));
        cacheService.saveShopInfo2RedisCache(shopInfo);
    }
}
