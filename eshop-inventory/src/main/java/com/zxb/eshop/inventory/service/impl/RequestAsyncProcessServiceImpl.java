package com.zxb.eshop.inventory.service.impl;

import com.zxb.eshop.inventory.request.ProductInventoryCacheRefreshRequest;
import com.zxb.eshop.inventory.request.Request;
import com.zxb.eshop.inventory.request.RequestQueue;
import com.zxb.eshop.inventory.service.RequestAsyncProcessService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by xuery on 2018/1/23.
 */
@Service
public class RequestAsyncProcessServiceImpl implements RequestAsyncProcessService {

    private  static final Logger logger = LoggerFactory.getLogger(RequestAsyncProcessServiceImpl.class);

    @Override
    public void process(Request request) {
        try{
            //根据商品id路由到对应的队列中
            ArrayBlockingQueue<Request> queue = getRoutingQueue(request.getProductId());
            logger.info(">>>>>RequestAsyncProcessServiceImpl-process, 路由到相应的队列，productId="+request.getProductId()+",uuid:"+ UUID.randomUUID());
            //读请求去重优化，如果是读请求并且队列中有对应的读请求则不用再发一次请求了
            //这里去重对应高并发是很有用的，如果一个热门商品库存瞬间几万的访问量并且刚好缓存中没有，如果全部放入队列，则靠后的人延迟可能非常大
            //queue的大小也就100，这时候阻塞会非常严重
            if(request instanceof ProductInventoryCacheRefreshRequest && queue.contains(request)){
                logger.info("读请求队列去重，productId:{}",request.getProductId());
                return;
            }
            //将请求放入对应的队列中，完成路由操作;采用put会阻塞，当队列满时，会一直等待直到可以插入队列
            queue.put(request);
        }catch (Exception e){
            logger.error("RequestAsyncProcessServiceImpl-process-error",e);
        }
    }

    /**
     * 获取路由到的内存队列
     *
     * @param productId
     * @return
     */
    private ArrayBlockingQueue<Request> getRoutingQueue(Integer productId) {
        RequestQueue requestQueue = RequestQueue.getInstance();

        //根据productId计算key
        String key = String.valueOf(productId);
        int h;
        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

        //对hash值取模，将hash值路由到制定的内存队列中，比如内存队列大小为8
        //可以保证同一个商品的id都会被固定路由到同样的一个内存队列中去的
        logger.info("requestQueue.size:"+ requestQueue.queueSize());
        int index = (requestQueue.queueSize() - 1) & hash;
        logger.info(">>>>>计算产品id对应的路由队列号"+index);
        return requestQueue.getQueue(index);
    }
}
