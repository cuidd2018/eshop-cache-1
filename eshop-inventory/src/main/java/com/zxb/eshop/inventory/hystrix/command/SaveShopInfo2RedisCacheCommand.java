package com.zxb.eshop.inventory.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.zxb.eshop.inventory.common.RedisKeyPrefixConstant;
import com.zxb.eshop.inventory.model.ShopInfo;
import com.zxb.eshop.inventory.spring.SpringContext;
import redis.clients.jedis.JedisCluster;

/**
 * Created by xuery on 2018/3/16.
 */
public class SaveShopInfo2RedisCacheCommand extends HystrixCommand<Boolean> {

    private ShopInfo shopInfo;

    public SaveShopInfo2RedisCacheCommand(ShopInfo shopInfo) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(200)
                        .withCircuitBreakerRequestVolumeThreshold(1000)
                        .withCircuitBreakerErrorThresholdPercentage(70)
                        .withCircuitBreakerSleepWindowInMilliseconds(60 * 1000)));
        this.shopInfo = shopInfo;
    }

    @Override
    protected Boolean run() throws Exception {
        JedisCluster jedisCluster = (JedisCluster) SpringContext.getApplicationContext().getBean("JedisClusterFactory");
        String key = RedisKeyPrefixConstant.SHOP_INFO + shopInfo.getId();
        System.out.println("SaveShopInfo2RedisCacheCommand start=====");
        jedisCluster.set(key, JSONObject.toJSONString(shopInfo));
        return true;
    }

    @Override
    protected Boolean getFallback() {
        System.out.println("SaveShopInfo2RedisCacheCommand降级处理");
        return null;
    }
}
