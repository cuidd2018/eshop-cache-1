package com.zxb.eshop.inventory.listener;

import com.zxb.eshop.inventory.kafka.KafkaConsumer;
import com.zxb.eshop.inventory.rebuild.RebuildCacheThread;
import com.zxb.eshop.inventory.spring.SpringContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * kafka系统初始化监听器，主要是注册消费者
 * Created by xuery on 2018/1/26.
 */
public class KafkaInitListener implements ServletContextListener{

    private static final Logger logger = LoggerFactory.getLogger(KafkaInitListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //获取ApplicationContext，自动注入类，因为在kafkaConsumer中会用到CacheService
        //如果没有这段代码，里面的CacheService是空的，会报空指针错误,通过这种方法调用getBean()即可获取
        ServletContext sc = servletContextEvent.getServletContext();
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(sc);
        SpringContext.setApplicationContext(context);

        //开启一个线程注册一个消费者
        logger.info("注册一个消费者start");
        new Thread(new KafkaConsumer("cache-product-info")).start();
        //开启一个线程从队列中取出商品信息更新缓存
        logger.info("开启一个线程用于商品信息缓存更新");
        new Thread(new RebuildCacheThread()).start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
