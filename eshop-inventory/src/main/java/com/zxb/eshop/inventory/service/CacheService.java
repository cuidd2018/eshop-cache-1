package com.zxb.eshop.inventory.service;

import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.model.ShopInfo;

/**
 * ehcache本地缓存service接口
 * Created by xuery on 2018/1/24.
 */
public interface CacheService {

    /**
     * 根据商品id查询本地缓存商品信息
     * @param id
     * @return
     */
    ProductInfo getProductInfoFromLocalCache(Long id);

    /**
     * 保存商品信息到本地缓存
     * @param productInfo
     * @return
     */
    ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo);

    /**
     * 根据店铺id查询本地缓存商品信息
     * @param id
     * @return
     */
    ShopInfo getShopInfoFromLocalCache(Long id);

    /**
     * 保存店铺信息到本地缓存
     * @param shopInfo
     * @return
     */
    ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo);

    /**
     * 保存商品信息保存到redis缓存
     * @param productInfo
     * @return
     */
    void saveProductInfo2RedisCache(ProductInfo productInfo);

    /**
     * 保存店铺信息保存到redis缓存
     * @param shopInfo
     * @return
     */
    void saveShopInfo2RedisCache(ShopInfo shopInfo);

    /**
     * 从redis缓存中获取商品信息
     * @param productId
     * @return
     */
    ProductInfo getProductInfoFromRedisCache(Long productId);

    /**
     * 从redis缓存中获取店铺信息
     * @param shopId
     * @return
     */
    ShopInfo getShopInfoFromRedisCache(Long shopId);

}
