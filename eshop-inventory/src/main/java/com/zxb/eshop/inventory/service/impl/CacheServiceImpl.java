package com.zxb.eshop.inventory.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.zxb.eshop.inventory.dao.RedisDAO;
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

    public static final String CACHE_NAME = "local";
    public static final String PRODUCT_INFO = "product_info_";
    public static final String SHOP_INFO = "shop_info_";

    //value指定ehcache中配置的缓存策略，@Cacheable不覆盖k-v(相当于get，只有第一次会写缓存),@CachePut会覆盖k-v(相当于set，每次都会写缓存)
    @Override
    @Cacheable(value = CACHE_NAME, key = "'key_'+#id")
    public ProductInfo getProductInfoFromLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'key_'+#productInfo.getId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'key_'+#id")
    public ShopInfo getShopInfoFromLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "'key_'+#shopInfo.getId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo) {
        return shopInfo;
    }

    @Override
    public void saveProductInfo2RedisCache(ProductInfo productInfo) {
        String key = PRODUCT_INFO + productInfo.getId();
        redisDAO.set(key, JSONObject.toJSONString(productInfo));
    }

    @Override
    public void saveShopInfo2RedisCache(ShopInfo shopInfo) {
        String key = SHOP_INFO + shopInfo.getId();
        redisDAO.set(key, JSONObject.toJSONString(shopInfo));
    }

    @Override
    public ProductInfo getProductInfoFromRedisCache(Long productId) {
        String key = PRODUCT_INFO + productId;
        String productInfoJson = redisDAO.get(key);
        if(StringUtils.isBlank(productInfoJson)){
            return null;
        }
        return JSONObject.parseObject(productInfoJson, ProductInfo.class);
    }

    @Override
    public ShopInfo getShopInfoFromRedisCache(Long shopId) {
        String key = SHOP_INFO + shopId;
        String shopInfoJson = redisDAO.get(key);
        if(StringUtils.isBlank(shopInfoJson)){
            return null;
        }
        return JSONObject.parseObject(shopInfoJson, ShopInfo.class);
    }
}
