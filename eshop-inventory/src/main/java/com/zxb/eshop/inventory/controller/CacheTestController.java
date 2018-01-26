package com.zxb.eshop.inventory.controller;

import com.zxb.eshop.inventory.model.ProductInfo;
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
public class CacheTestController {

    private static final Logger logger = LoggerFactory.getLogger(CacheTestController.class);

    @Autowired
    private CacheService cacheService;

    @RequestMapping("testPutCache")
    @ResponseBody
    public void testPutCache(ProductInfo productInfo){
        logger.info(">>>testPutCache:"+productInfo);
        cacheService.saveLocalCache(productInfo);
    }

    @RequestMapping("testGetCache")
    @ResponseBody
    public ProductInfo testGetCache(Long id){
        ProductInfo productInfo = cacheService.getLocalCache(id);
        logger.info(">>>testGetCache:"+productInfo);
        return productInfo;
    }
}
