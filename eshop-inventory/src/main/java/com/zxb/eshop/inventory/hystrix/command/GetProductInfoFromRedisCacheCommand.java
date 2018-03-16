package com.zxb.eshop.inventory.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.zxb.eshop.inventory.common.RedisKeyPrefixConstant;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.spring.SpringContext;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCluster;

/**
 * Created by xuery on 2018/3/16.
 */
public class GetProductInfoFromRedisCacheCommand extends HystrixCommand<ProductInfo>{

    private Long productId;

    public GetProductInfoFromRedisCacheCommand(Long productId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(200)
                        .withCircuitBreakerRequestVolumeThreshold(1000)
                        .withCircuitBreakerErrorThresholdPercentage(70)
                        .withCircuitBreakerSleepWindowInMilliseconds(60 * 1000)));
        this.productId = productId;
    }

    @Override
    protected ProductInfo run() throws Exception {
        JedisCluster jedisCluster = (JedisCluster) SpringContext.getApplicationContext().getBean("JedisClusterFactory");
        String key = RedisKeyPrefixConstant.PRODUCT_INFO + productId;
        String productInfoJson = jedisCluster.get(key);
        if(StringUtils.isBlank(productInfoJson)){
            return null;
        }
        return JSONObject.parseObject(productInfoJson, ProductInfo.class);
    }

    @Override
    protected ProductInfo getFallback() {
        System.out.println("GetProductInfoFromRedisCacheCommand降级处理");
        return null;
    }
}
