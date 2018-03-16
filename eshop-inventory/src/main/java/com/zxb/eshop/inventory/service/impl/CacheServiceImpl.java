package com.zxb.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.common.RedisKeyPrefixConstant;
import com.zxb.eshop.inventory.dao.RedisDAO;
import com.zxb.eshop.inventory.hystrix.command.GetProductInfoFromRedisCacheCommand;
import com.zxb.eshop.inventory.hystrix.command.GetShopInfoFromRedisCacheCommand;
import com.zxb.eshop.inventory.hystrix.command.SaveProductInfo2RedisCacheCommand;
import com.zxb.eshop.inventory.hystrix.command.SaveShopInfo2RedisCacheCommand;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.model.ShopInfo;
import com.zxb.eshop.inventory.service.CacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by xuery on 2018/1/24.
 */
@Service("cacheService")
public class CacheServiceImpl implements CacheService {

    @Autowired
    private RedisDAO redisDAO;

    //value指定ehcache中配置的缓存策略，@Cacheable不覆盖k-v(相当于get，只有第一次会写缓存),@CachePut会覆盖k-v(相当于set，每次都会写缓存)
    @Override
    @Cacheable(value = RedisKeyPrefixConstant.CACHE_NAME, key = "'key_'+#id")
    public ProductInfo getProductInfoFromLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value = RedisKeyPrefixConstant.CACHE_NAME, key = "'key_'+#productInfo.getId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    @Override
    @Cacheable(value = RedisKeyPrefixConstant.CACHE_NAME, key = "'key_'+#id")
    public ShopInfo getShopInfoFromLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value = RedisKeyPrefixConstant.CACHE_NAME, key = "'key_'+#shopInfo.getId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
        return shopInfo;
    }

    /**
     *将一下四个方法改造成hystrixCommand调用
     * 20180316 xuery
     */
    @Override
    public void saveProductInfo2RedisCache(ProductInfo productInfo) {
        SaveProductInfo2RedisCacheCommand saveProductInfo2RedisCacheCommand = new SaveProductInfo2RedisCacheCommand(productInfo);
        Boolean result = saveProductInfo2RedisCacheCommand.execute();
    }

    @Override
    public void saveShopInfo2RedisCache(ShopInfo shopInfo) {
        SaveShopInfo2RedisCacheCommand saveShopInfo2RedisCacheCommand = new SaveShopInfo2RedisCacheCommand(shopInfo);
        Boolean result = saveShopInfo2RedisCacheCommand.execute();
    }

    @Override
    public ProductInfo getProductInfoFromRedisCache(Long productId) {
        GetProductInfoFromRedisCacheCommand getProductInfoFromRedisCacheCommand = new GetProductInfoFromRedisCacheCommand(productId);
        return getProductInfoFromRedisCacheCommand.execute();
    }

    @Override
    public ShopInfo getShopInfoFromRedisCache(Long shopId) {
        GetShopInfoFromRedisCacheCommand getShopInfoFromRedisCacheCommand = new GetShopInfoFromRedisCacheCommand(shopId);
        return getShopInfoFromRedisCacheCommand.execute();
    }
}
