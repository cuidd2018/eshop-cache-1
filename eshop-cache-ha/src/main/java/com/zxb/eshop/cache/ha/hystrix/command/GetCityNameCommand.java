package com.zxb.eshop.cache.ha.hystrix.command;

import com.netflix.hystrix.*;
import com.zxb.eshop.cache.ha.cache.local.LocationCache;

/**
 * Created by xuery on 2018/2/28.
 */
public class GetCityNameCommand extends HystrixCommand<String> {

    private Long cityId;

    public GetCityNameCommand(Long cityId){
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("GetCityNameGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("GetCityNameCommand"))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("GetCityNamePool"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(15) //指定最大信号量数
                )
        );
        this.cityId = cityId;

    }

    @Override
    protected String run() throws  Exception{
        return LocationCache.getCityName(cityId);
    }
}
