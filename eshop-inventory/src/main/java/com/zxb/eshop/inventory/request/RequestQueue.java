package com.zxb.eshop.inventory.request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 请求内存队列
 * Created by xuery on 2018/1/23.
 */
public class RequestQueue {

    /**
     * 内存队列
     * 把10个内存队列组合起来放到一个list中
     */
    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<>();

    /**
     * 单例
     * 静态内部类的方式
     */
    private static class Singleton {
        private static RequestQueue instance;

        static{
            instance = new RequestQueue();
        }

        public static RequestQueue getInstance() {
            return instance;
        }
    }

    /**
     * jvm机制去保证多线程并发安全
     *
     * @return
     */
    public static RequestQueue getInstance(){
        return Singleton.getInstance();
    }

    /**
     * 添加一个内存队列
     */
    public void addQueue(ArrayBlockingQueue<Request> queue){
        this.queues.add(queue);
    }

    /**
     *队列总数
     */
    public int queueSize(){
        return queues.size();
    }

    public ArrayBlockingQueue<Request> getQueue(int index){
        return queues.get(index);
    }
}
