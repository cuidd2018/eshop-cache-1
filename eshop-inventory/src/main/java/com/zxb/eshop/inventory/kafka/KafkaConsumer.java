package com.zxb.eshop.inventory.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by xuery on 2018/1/26.
 */
public class KafkaConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final ConsumerConnector consumerConnector;

    private final String topic;

    public KafkaConsumer(String topic) {
        logger.info("注册一个消费者");
        this.consumerConnector = Consumer.createJavaConsumerConnector(createConsumerConfig());
        this.topic = topic;
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

    @Override
    public void run() {
        //1.指定topic
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);

        // 2. 获取连接数据的迭代器对象集合
        /**
         * Key: Topic主题
         * Value: 对应Topic的数据流读取器，大小是topicCountMap中指定的topic大小,这里设置为1
         */
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        // 3. 从返回结果中获取对应topic的数据流处理器
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        for (KafkaStream stream : streams){
            new Thread(new KafkaMessageProcessor(stream)).start(); //开启线程消费数据
        }

    }
}
