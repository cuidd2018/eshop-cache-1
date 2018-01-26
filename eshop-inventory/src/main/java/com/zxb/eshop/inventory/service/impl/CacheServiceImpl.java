package com.zxb.eshop.inventory.service.impl;

import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.service.CacheService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Created by xuery on 2018/1/24.
 */
@Service
public class CacheServiceImpl implements CacheService {

    public static final String CACHE_NAME = "local";

    //value指定ehcache中配置的缓存策略，@Cacheable不覆盖k-v(相当于get，只有第一次会写缓存),@CachePut会覆盖k-v(相当于set，每次都会写缓存)
    @Override
    @Cacheable(value=CACHE_NAME,key="'key_'+#id")
    public ProductInfo getLocalCache(Long id) {
        return null;
    }

    @Override
    @CachePut(value=CACHE_NAME,key="'key_'+#productInfo.getId()")
    public ProductInfo saveLocalCache(ProductInfo productInfo) {
        return productInfo;
    }
}
