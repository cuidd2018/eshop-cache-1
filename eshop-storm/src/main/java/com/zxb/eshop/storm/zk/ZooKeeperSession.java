package com.zxb.eshop.storm.zk;

import org.apache.storm.utils.Utils;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by xuery on 2018/2/1.
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
     * @param path
     */
    public void aquireDistributedLock(String path) {
        try {
            //ZooDefs.Ids.OPEN_ACL_UNSAFE:任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
            //CreateMode.EPHEMERAL: The znode will be deleted upon the client's disconnect.
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("成功获取zk锁, path={}", path);
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
                logger.info("成功获取zk锁, path={}, after {} time retries", path, count);
                break;
            }
        }
    }

    public void createNode(String path){
        try {
            //ZooDefs.Ids.OPEN_ACL_UNSAFE:任何应用程序在节点上可进行任何操作，能创建、列出和删除它的子节点
            //CreateMode.EPHEMERAL: The znode will be deleted upon the client's disconnect.
            zookeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("成功创建node, path={}", path);
        } catch (Exception e) {
            logger.error("zk node path={} has already exists");
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

    /**
     * 删除node
     *
     * @param path
     */
    public void deleteZkNode(String path) {
        try {
            zookeeper.delete(path, -1);
        } catch (Exception e) {
            logger.error("deleteZkNode error,path={}, node doesn't exist", path);
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
            logger.error("releaseDistributedLock error,path={}, errorMsg:", path, e);
        }
    }

    /**
     * 封装单例的静态内部类
     *
     * @author Administrator
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
     *
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

