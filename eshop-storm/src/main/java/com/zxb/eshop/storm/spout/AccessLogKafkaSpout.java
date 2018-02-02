package com.zxb.eshop.storm.spout;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * spout消费kafka数据
 * Created by xuery on 2018/1/31.
 */
public class AccessLogKafkaSpout extends BaseRichSpout {
    private static final Logger logger = LoggerFactory.getLogger(AccessLogKafkaSpout.class);

    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(1000);

    private SpoutOutputCollector collector;

    @Override
    public void open(Map map, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        //注册一个kafka消费者，当有消费数据时写入队列
        this.startKafkaConsumer();
    }

    private void startKafkaConsumer() {
        //1.创建消费者连接对象
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(createConsumerConfig());
        //2.指定topic
        String topic = "access-log";
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);

        // 3. 获取连接数据的迭代器对象集合
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        // 4. 从返回结果中获取对应topic的数据流处理器
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        for (KafkaStream stream : streams) {
            new Thread(new KafkaMessageProcessor(stream)).start(); //开启线程消费数据
        }

    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.95.135:2181,192.168.95.137:2181,192.168.95.138:2181");
        props.put("group.id", "eshop-cache-group");
        props.put("zookeeper.session.timeout.ms", "40000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        return new ConsumerConfig(props);
    }

    /**
     * kafka消费者消费线程
     */
    private class KafkaMessageProcessor implements Runnable {

        private KafkaStream kafkaStream;

        public KafkaMessageProcessor(KafkaStream kafkaStream) {
            this.kafkaStream = kafkaStream;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
            while (it.hasNext()) {

                String message = new String(it.next().message());
                try {
                    queue.put(message);
                } catch (Exception e) {
                    logger.error("KafkaMessageProcessor run error:{}", e);
                }
            }
        }
    }


    @Override
    public void nextTuple() {
        //storm会一直去执行里面的代码，可以通过加条件让其有条件的执行
        if (queue.size() > 0) {
            try {
                String message = queue.take();
                logger.info(">>>accessLogKafkaSpout接收到kafka商品访问信息并发射出去:{}", message);
                collector.emit(new Values(message));
            } catch (Exception e) {
                logger.error("AccessLogKafkaSpout nextTuple error:{}", e);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("message"));
    }
}
