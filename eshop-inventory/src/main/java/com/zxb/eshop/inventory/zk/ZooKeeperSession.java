package com.zxb.eshop.inventory.zk;

import com.zxb.eshop.inventory.utils.SleepUtil;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * 创建zk分布式锁类
 * Created by xuery on 2018/1/29.
 */
public class ZooKeeperSession {

    Logger logger = LoggerFactory.getLogger(ZooKeeperSession.class);

    //用于不同线程之间的等待
    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private ZooKeeper zookeeper;

    public ZooKeeperSession() {
        try {
            this.zookeeper = new ZooKeeper(
                    "192.168.95.135:2181,192.168.95.137:2181,192.168.95.138:2181",
                    50000,
                    new Watcher() {
                        /**
                         * 用于监听是否连接zookeeper成功
                         * @param watchedEvent
                         */
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            logger.info("receive watched event:" + watchedEvent.getState());
                            if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                                connectedSemaphore.countDown();
                            }
                        }
                    });
            logger.info("zookeeper初始状态值为：" + zookeeper.getState());

            connectedSemaphore.await(); //这里hang住，等待连接建立

            logger.info("zookeeper连接已建立！");
        } catch (Exception e) {
            logger.error("zookeeper建立连接出错，error:", e);
        }
    }


    /**
     * 获取分布式锁
     *
     * @param productId
     */
    public void aquireDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            //ZooDefs.Ids.OPEN_ACL_UNSAFE:任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
            //CreateMode.EPHEMERAL: The znode will be deleted upon the client's disconnect.
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("success acquire zookeeper lock, productId={}", productId);
        } catch (Exception e) {
            // 如果那个商品对应的锁的node，已经存在了，就是已经被别人加锁了，那么就这里就会报错
            // NodeExistsException
            int count = 0;
            while (true) {
                try {
                    SleepUtil.pause(20);
                    zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e1) {
                    count++;
                    logger.info("try to get zk lock ...{}",count);
                    continue;
                }
                logger.info("success acquire zookeeper lock, productId={}, after {} time retries", productId, count);
                break;
            }
        }
    }

    /**
     * 释放分布式锁
     *
     * @param productId
     */
    public void releaseDistributedLock(Long productId) {
        String path = "/product-lock-" + productId;
        try {
            zookeeper.delete(path, -1);
        } catch (Exception e) {
            logger.error("releaseDistributedLock error, productId={}, errorMsg:", productId, e);
        }
    }

    /**
     * 封装单例的静态内部类
     * @author Administrator
     *
     */
    private static class Singleton {

        private static ZooKeeperSession instance;

        static {
            instance = new ZooKeeperSession();
        }

        public static ZooKeeperSession getInstance() {
            return instance;
        }

    }

    /**
     * 获取单例
     * @return
     */
    public static ZooKeeperSession getInstance() {
        return Singleton.getInstance();
    }

    /**
     * 初始化单例的便捷方法
     */
    public static void init() {
        getInstance();
    }

}
