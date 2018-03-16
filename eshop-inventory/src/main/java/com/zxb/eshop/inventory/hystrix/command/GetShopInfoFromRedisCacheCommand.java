package com.zxb.eshop.inventory.hystrix.command;

import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.zxb.eshop.inventory.common.RedisKeyPrefixConstant;
import com.zxb.eshop.inventory.model.ProductInfo;
import com.zxb.eshop.inventory.model.ShopInfo;
import com.zxb.eshop.inventory.spring.SpringContext;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisCluster;

/**
 * Created by xuery on 2018/3/16.
 */
public class GetShopInfoFromRedisCacheCommand extends HystrixCommand<ShopInfo>{

    private Long shopId;

    public GetShopInfoFromRedisCacheCommand(Long shopId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RedisGroup"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionTimeoutInMilliseconds(200)
                        .withCircuitBreakerRequestVolumeThreshold(1000)
                        .withCircuitBreakerErrorThresholdPercentage(70)
                        .withCircuitBreakerSleepWindowInMilliseconds(60 * 1000)));
        this.shopId = shopId;
    }

    @Override
    protected ShopInfo run() throws Exception {
        JedisCluster jedisCluster = (JedisCluster) SpringContext.getApplicationContext().getBean("JedisClusterFactory");
        String key = RedisKeyPrefixConstant.SHOP_INFO + shopId;
        String shopInfoJson = jedisCluster.get(key);
        if(StringUtils.isBlank(shopInfoJson)){
            return null;
        }
        return JSONObject.parseObject(shopInfoJson, ShopInfo.class);
    }

    @Override
    protected ShopInfo getFallback() {
        System.out.println("GetShopInfoFromRedisCacheCommand降级处理");
        return null;
    }
}
