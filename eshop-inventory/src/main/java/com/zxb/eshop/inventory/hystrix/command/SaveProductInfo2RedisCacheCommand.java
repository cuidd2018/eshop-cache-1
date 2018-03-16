package com.zxb.eshop.inventory.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.zxb.eshop.inventory.common.RedisKeyPrefixConstant;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.spring.SpringContext;
import redis.clients.jedis.JedisCluster;

/**
 * Created by xuery on 2018/3/16.
 */
public class SaveProductInfo2RedisCacheCommand extends HystrixCommand<Boolean> {

    private ProductInfo productInfo;

    public SaveProductInfo2RedisCacheCommand(ProductInfo productInfo) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(200)
                        .withCircuitBreakerRequestVolumeThreshold(1000)
                        .withCircuitBreakerErrorThresholdPercentage(70)
                        .withCircuitBreakerSleepWindowInMilliseconds(60 * 1000)
                ));
        this.productInfo = productInfo;
    }

    @Override
    protected Boolean run() throws Exception {
        JedisCluster jedisCluster = (JedisCluster) SpringContext.getApplicationContext().getBean("JedisClusterFactory");
        String key = RedisKeyPrefixConstant.PRODUCT_INFO + productInfo.getId();
        System.out.println("SaveProductInfo2RedisCacheCommand start=====");
        jedisCluster.set(key, JSONObject.toJSONString(productInfo));
        return true;
    }

    @Override
    protected Boolean getFallback() {
        System.out.println("SaveProductInfo2RedisCacheCommand降级处理");
        return false;
    }
}
