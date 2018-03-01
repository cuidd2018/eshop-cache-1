package com.zxb.eshop.cache.ha.hystrix.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Created by xuery on 2018/3/1.
 */
public class CommandUsingRequestCache extends HystrixCommand<Boolean> {

    private final int value;

    public CommandUsingRequestCache(int value) {
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.value = value;
    }

    @Override
    protected Boolean run() throws Exception {
        System.out.println("CommandUsingRequestCache run ..., value=" + value);
        return value == 0 || value % 2 == 0;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(value);
    }
}
