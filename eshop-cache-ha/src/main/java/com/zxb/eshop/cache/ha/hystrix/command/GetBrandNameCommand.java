package com.zxb.eshop.cache.ha.hystrix.command;

import com.netflix.hystrix.*;
import com.zxb.eshop.cache.ha.Utils.SleepUtils;
import com.zxb.eshop.cache.ha.cache.local.BrandCache;

/**
 * Created by xuery on 2018/3/1.
 */
public class GetBrandNameCommand extends HystrixCommand<String> {

    private Long brandId;

    public GetBrandNameCommand(Long brandId) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("BrandInfoService"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("GetBrandNameCommand"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("GetBrandInfoPool"))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                        .withCoreSize(15)
                        .withQueueSizeRejectionThreshold(10))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withFallbackIsolationSemaphoreMaxConcurrentRequests(15)) //设置fallback最大并发数，超过设定值则reject，是通过信号量来限流的
        );
        this.brandId = brandId;
    }

    @Override
    protected String run() throws Exception {
        //抛出异常则会去调用fallback逻辑
        throw new Exception();
    }

    @Override
    protected String getFallback() {
        SleepUtils.sleep(1000);
        System.out.println(Thread.currentThread().getName()+" 从本地缓存中获取过期数据，brandId=" + brandId);
        return BrandCache.getBrandName(brandId);
    }
}
