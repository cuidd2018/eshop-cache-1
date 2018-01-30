package com.zxb.eshop.inventory.rebuild;

import com.zxb.eshop.inventory.model.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 缓存重建内存队列
 * Created by xuery on 2018/1/29.
 */
public class RebuildCacheQueue {

    private static final Logger logger = LoggerFactory.getLogger(RebuildCacheQueue.class);

    /**
     *阻塞队列
     */
    private ArrayBlockingQueue<ProductInfo> queue = new ArrayBlockingQueue<ProductInfo>(100);

    /**
     * 入队列
     * @param productInfo
     */
    public void putProductInfo(ProductInfo productInfo){
        try {
            queue.put(productInfo);
        } catch (Exception e){
            logger.error("putProductInfo error, productId={}, error:",productInfo.getId(),e);
        }
    }

    /**
     * 出队列
     * @return
     */
    public ProductInfo takeProductInfo(){
        try{
            return queue.take();
        }catch (Exception e){
            logger.error("takeProductInfo error, productId={}, error:",e);
        }
        return null;
    }

    /**
     * 内部单例类
     * @author Administrator
     *
     */
    private static class Singleton {

        private static RebuildCacheQueue instance;

        static {
            instance = new RebuildCacheQueue();
        }

        public static RebuildCacheQueue getInstance() {
            return instance;
        }

    }

    public static RebuildCacheQueue getInstance() {
        return Singleton.getInstance();
    }

    public static void init() {
        getInstance();
    }

}
