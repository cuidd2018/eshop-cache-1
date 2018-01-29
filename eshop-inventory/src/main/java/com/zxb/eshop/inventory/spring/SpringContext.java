package com.zxb.eshop.inventory.spring;

import org.springframework.context.ApplicationContext;

/**
 * Spring上下文
 * Created by xuery on 2018/1/27.
 */
public class SpringContext {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContext.applicationContext = applicationContext;
    }
}
