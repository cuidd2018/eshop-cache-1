package com.zxb.eshop.storm.bolt;

import com.alibaba.fastjson.JSONObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 从message json串中提取producId
 * Created by xuery on 2018/1/31.
 */
public class LogParseBolt extends BaseRichBolt {

    private static final Logger logger = LoggerFactory.getLogger(LogParseBolt.class);

    private OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        String message = tuple.getStringByField("message");
        logger.info(">>>LogParseBolt接收到上一级发射的商品访问信息为:{}", message);
        JSONObject messageJSON = JSONObject.parseObject(message);
        JSONObject uriArgsJSON = messageJSON.getJSONObject("uri_args");
        Long productId = uriArgsJSON.getLong("productId");
        logger.info(">>>LogParseBolt发射到下一级的商品id为：{}", productId);
        if (productId != null) {
            collector.emit(new Values(productId));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("productId"));
    }
}
