package com.zxb.eshop.inventory.service;

import com.zxb.eshop.inventory.model.ProductInfo;

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
    ProductInfo getLocalCache(Long id);

    /**
     * 保存商品信息到本地缓存
     * @param productInfo
     * @return
     */
    ProductInfo saveLocalCache(ProductInfo productInfo);
}
