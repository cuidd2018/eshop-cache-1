package com.zxb.eshop.inventory.listener;

import com.zxb.eshop.inventory.thread.RequestProcessorThreadPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 系统初始化监听器
 * Created by xuery on 2018/1/23.
 */
public class RequestProcessorInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //初始化工作线程和内存队列
        /**
         * 初始化了一个请求线程池
         * 线程池开启了10个线程
         * 每个线程有一个容量100的阻塞队列
         * 并把10个线程的阻塞队列都加入一个请求队列中RequestQueue, 用于做队列路由使用，保证同一个productId被路由到同一个queue中
         */
        RequestProcessorThreadPool.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
