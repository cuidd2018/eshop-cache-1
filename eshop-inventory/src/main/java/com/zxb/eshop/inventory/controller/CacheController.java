package com.zxb.eshop.inventory.controller;

import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.model.ShopInfo;
import com.zxb.eshop.inventory.prewarm.CachePrewarmThread;
import com.zxb.eshop.inventory.rebuild.RebuildCacheQueue;
import com.zxb.eshop.inventory.service.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuery on 2018/1/24.
 */
@Controller
public class CacheController {

    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheService cacheService;

    @RequestMapping("testPutCache")
    @ResponseBody
    public void testPutCache(ProductInfo productInfo) {
        logger.info(">>>testPutCache:" + productInfo);
        cacheService.saveProductInfo2LocalCache(productInfo);
    }

    @RequestMapping("testGetCache")
    @ResponseBody
    public ProductInfo testGetCache(Long id) {
        ProductInfo productInfo = cacheService.getProductInfoFromLocalCache(id);
        logger.info(">>>testGetCache:" + productInfo);
        return productInfo;
    }

    @RequestMapping("getProductInfo")
    @ResponseBody
    public ProductInfo getProductInfo(long productId) {
        logger.info(">>>getProductInfo start, productId={}", productId);
        ProductInfo productInfo = cacheService.getProductInfoFromRedisCache(productId);
        logger.info(">>>getProductInfo,productId={},redis缓存商品内容为:{}", productId, productInfo);
        if (productInfo == null) {
            productInfo = cacheService.getProductInfoFromLocalCache(productId);
            logger.info(">>>getProductInfo,productId={},ehcache本地缓存商品内容为:{}", productId, productInfo);
        }
        if (productInfo == null) {
            //todo xuery 这时候只能从数据库中查了,这个逻辑先待定
            //这里模拟并发缓存重建,这里直接写死，实际中需要从mysql中读取
            String productInfoJSON = "{\"id\": 6, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
            productInfo = JSONObject.parseObject(productInfoJSON, ProductInfo.class);
            RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
            rebuildCacheQueue.putProductInfo(productInfo);
        }
        return productInfo;
    }

    @RequestMapping("getShopInfo")
    @ResponseBody
    public ShopInfo getShopInfo(long shopId) {
        logger.info(">>>getShopInfo start, shopId={}", shopId);
        ShopInfo shopInfo = cacheService.getShopInfoFromRedisCache(shopId);
        logger.info(">>>getShopInfo,shopId={},redis缓存店铺内容为:{}", shopId, shopInfo);
        if (shopInfo == null) {
            shopInfo = cacheService.getShopInfoFromLocalCache(shopId);
            logger.info(">>>getShopInfo,shopId={},ehcache缓存店铺内容为:{}", shopId, shopInfo);
        }
        if (shopInfo == null) {
            //todo xuery 从数据库中查询
        }
        return shopInfo;
    }

    /**
     * 热门商品预热，暂时只做成这种手动触发的，实际中可以采用Scheduler定时器去预热
     */
    @RequestMapping("prewarmCache")
    @ResponseBody
    public void prewarmCache() {
        for (int i = 0; i < 3; i++) {
            new Thread(new CachePrewarmThread()).start();
        }
    }
}
