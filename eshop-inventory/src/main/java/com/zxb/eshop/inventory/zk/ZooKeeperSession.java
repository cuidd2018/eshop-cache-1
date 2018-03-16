package com.zxb.eshop.inventory.zk;

import com.zxb.eshop.inventory.common.utils.SleepUtil;
import org.apache.storm.utils.Utils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
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
     * 获取分布式锁
     * 只获取一次，未获取到则下面的操作不执行，获取到则执行；保证同一个taskId上传到的同一批热门商品只被预热一次
     */
    public boolean aquireFastFailDistributedLock(String path) {
        try {
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("aquireFastFailDistributedLock--成功获取zk锁, path={}", path);
            return true;
        } catch (Exception e) {
            logger.info("aquireFastFailDistributedLock--获取zk锁失败, path={}", path);
        }
        return false;
    }

    /**
     * 获取分布式锁
     *
     * @param path
     */
    public void aquireDistributedLock(String path) {
        try {
            //ZooDefs.Ids.OPEN_ACL_UNSAFE:任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
            //CreateMode.EPHEMERAL: The znode will be deleted upon the client's disconnect.
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("aquireDistributedLock--成功获取zk锁, path={}", path);
        } catch (Exception e) {
            //这里如果没获取到，说明有其他task获取到了，如果是写taskId list则需要轮询直至获得锁
            int count = 0;
            while (true) {
                try {
                    Utils.sleep(1000); //这个时间需要根据不同的场景调整，如果无法统一则需要写多个acquireDistributedLock
                    zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e1) {
                    count++;
                    logger.info("try to get zk lock {}...{}", path, count);
                    continue;
                }
                logger.info("aquireDistributedLock--成功获取zk锁, path={}, after {} time retries", path, count);
                break;
            }
        }
    }

    public String getNodeData(String path) {
        try {
            return new String(zookeeper.getData(path, false, new Stat()));
        } catch (Exception e) {
            logger.error("zk getNodeData error, path={}", path, e);
        }
        return "";
    }

    public void setNodeData(String path, String data) {
        try {
            zookeeper.setData(path, data.getBytes(), -1);
        } catch (Exception e) {
            logger.error("zk setNodeData error, path={}, data={}", path, data);
        }
    }

    public void createNode(String path){
        try {
            //ZooDefs.Ids.OPEN_ACL_UNSAFE:任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
            //CreateMode.EPHEMERAL: The znode will be deleted upon the client's disconnect.
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("success create node, path={}", path);
        } catch (Exception e) {
            logger.error("zk node path={} has already exists");
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
     * 释放分布式锁
     *
     * @param path
     */
    public void releaseDistributedLock(String path) {
        try {
            zookeeper.delete(path, -1);
        } catch (Exception e) {
            logger.error("releaseDistributedLock error, path={}, errorMsg:", path, e);
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
