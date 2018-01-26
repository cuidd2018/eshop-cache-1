package com.zxb.eshop.inventory.thread;

import com.zxb.eshop.inventory.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * 执行请求的工作线程
 * Created by xuery on 2018/1/23.
 */
public class RequestProcessorThread implements Callable<Boolean>{

    private static final Logger logger = LoggerFactory.getLogger(RequestProcessorThread.class);

    /**
     *自己监控的内存队列
     */
    private ArrayBlockingQueue<Request> queue;

    public RequestProcessorThread(ArrayBlockingQueue<Request> queue) {
        this.queue = queue;
    }

    @Override
    public Boolean call() {
        try {
            while (true) {
                //在这里处理请求，不断轮询请求, 要考量下这种死轮询的开销大不大
                //与put配套，都是阻塞的
                Request request = queue.take();
                //执行对应的process方法即可
                request.process();
            }
        } catch (Exception e){
            logger.error("RequestProcessorThread--call--error",e);
        }
        return true;
    }
}
