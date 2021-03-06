package test.org.evan.libraries.rocketmq.support.consumer;

/**
 * @author Evan.Shen
 * @since 2019-08-19
 */


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.evan.libraries.rocketmq.ConsumerConfig;
import org.evan.libraries.rocketmq.ConsumerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Component
//@RocketMQMessageListener(topic = "TEST_2_TOPIC", consumerGroup = "consumer2Group", consumeMode = ConsumeMode.CONCURRENTLY, messageModel = MessageModel.CLUSTERING)
public class ConsumerTopic2Test {
    @Autowired
    private RocketMQProperties rocketMQProperties;

    private DefaultMQPushConsumer consumer;

    @Autowired
    private ConsumerDataStater consumerDataStat;

    @PostConstruct
    public void init() throws MQClientException {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setNameServer(rocketMQProperties.getNameServer());
        consumerConfig.setGroup("consumer2Group");
        consumerConfig.setTopic("TEST_2_TOPIC");

        consumer = ConsumerFactory.create(consumerConfig, new ListenterForDemo(consumerDataStat));
        consumer.setConsumeThreadMin(8);
        consumer.setConsumeThreadMax(16);
        consumer.start();
    }

    @PreDestroy
    public void shutdown() {
        consumer.shutdown();
    }
}


