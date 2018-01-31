package com.zxb.storm.wordcount.topology;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by xuery on 2018/1/30.
 * 单词统计拓扑类
 * <p>
 * spout-->bolt-->bolt, 流式计算结果
 */
public class WordCountTopology {

    public static class RandomSentenceSpout extends BaseRichSpout {
        private static final Logger logger = LoggerFactory.getLogger(RandomSentenceSpout.class);
        SpoutOutputCollector collector;
        Random random;


        /**
         * open方法用于对spout初始化，如创建线程池或者创建数据库连接等
         *
         * @param map
         * @param context
         * @param collector
         */
        @Override
        public void open(Map map, TopologyContext context, SpoutOutputCollector collector) {
            this.collector = collector;
            this.random = new Random();
        }

        /**
         * nextTuple
         * 这个spout最终会运行在一个task中，某个worker的某个executor的某个task中
         * 这个task会不断的无线循环的去调用nextTuple，形成一个数据流
         */
        @Override
        public void nextTuple() {
            Utils.sleep(100); //每隔1000ms跑一次
            String[] sentences = new String[]{"the cow jumped over the moon", "an apple a day keeps the doctor away", "four score and seven years ago",
                    "snow white and the seven dwarfs", "i am at two with nature"};
            String sentence = sentences[random.nextInt(sentences.length)];
            logger.info("spout发射出去的句子为：{}", sentence);
            collector.emit(new Values(sentence)); //构建一个Tuple发射出去，最小的数据单位，无线多个tuple构成一个stream
        }

        /**
         * 定义发射出去的每个tuple的每个field的名称是什么
         * 指定名称用于下一级获取相应field的数据
         *
         * @param declarer
         */
        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("sentence"));
        }
    }

    /**
     * bolt类，代码也是执行在某个worker的某个executor的某个task中
     */
    public static class SplitSentence extends BaseRichBolt {

        private static final Logger logger = LoggerFactory.getLogger(SplitSentence.class);

        OutputCollector collector; //发射器

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple tuple) {
            String sentence = tuple.getStringByField("sentence");
            String[] words = sentence.split(" ");
            logger.info("bolt receive sentence:{}",sentence);
            for(String word:words){
                collector.emit(new Values(word));
            }
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word"));
        }
    }

    public static class WordCount extends BaseRichBolt {

        private static final Logger logger = LoggerFactory.getLogger(WordCount.class);

        OutputCollector collector;

        private Map<String, Long> mapCount = new HashMap<>();

        @Override
        public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple tuple) {
            String word = tuple.getStringByField("word");
            long count = 0L;
            if(mapCount.containsKey(word)){
                count = mapCount.get(word) + 1L;
            } else {
                count = 1L;
            }
            mapCount.put(word, count);
            logger.info("【单词{}】，出现的次数为：{}", word, count);

            collector.emit(new Values(word,count));
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("word","count"));
        }
    }

    public static void main(String[] args) {
        //将spout/bolt串成一个拓扑，让数据流起来
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("randomSentence",new RandomSentenceSpout(),1); //2代表开启两个executor
        builder.setBolt("splitSentence", new SplitSentence(),2)
                .setNumTasks(4)                    //一个executor开启10个task
                .shuffleGrouping("randomSentence"); //上一级task到下一级task的映射规则为随机
        builder.setBolt("wordCount",new WordCount(),4)
                .setNumTasks(8)
                .fieldsGrouping("splitSentence", new Fields("word")); //这里必须定为按照单词来路由到下一级task，统计单词次数必须保证同一个单词进入同一个task

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
            cluster.submitTopology("wordCountTopology", config, builder.createTopology());

            Utils.sleep(50000); //跑60s
            cluster.shutdown();
        }
    }
}
