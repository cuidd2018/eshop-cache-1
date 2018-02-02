package com.zxb.eshop.storm;

import com.zxb.eshop.storm.bolt.LogParseBolt;
import com.zxb.eshop.storm.bolt.ProductCountBolt;
import com.zxb.eshop.storm.spout.AccessLogKafkaSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.utils.Utils;

/**
 * Created by xuery on 2018/1/31.
 */
public class HotProductTopology {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("accessLogMessage", new AccessLogKafkaSpout(), 1);
        builder.setBolt("logParseProductId", new LogParseBolt(), 2)
                .setNumTasks(2)
                .shuffleGrouping("accessLogMessage");
        builder.setBolt("productCount", new ProductCountBolt(), 2)
                .setNumTasks(2)
                .fieldsGrouping("logParseProductId", new Fields("productId"));

        Config config = new Config();
        if(args != null && args.length > 0){
            //命令行执行
            config.setNumWorkers(1); //设置进程数
            try{
                StormSubmitter.submitTopology(args[0], config, builder.createTopology());
            }catch (Exception e){
                e.printStackTrace();
            }
        } else{
            //在ide中执行
            config.setMaxTaskParallelism(20); //一个进程，设置20个executor执行
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("hotProductTopology", config, builder.createTopology());

            Utils.sleep(50000); //跑60s
            cluster.shutdown();
        }
    }
}
