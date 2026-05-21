package com.sprint.mission.discodeit.event.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * WS/SSE 푸시는 인스턴스마다 수신해야 하므로, 도메인 알림용 {@code discodeit-group}과 별도의 consumer group id를 둡니다.
 */
@Configuration
@ConditionalOnBean(KafkaTemplate.class)
public class RealtimePushKafkaConfig {

    @Bean
    public ConsumerFactory<String, String> realtimePushConsumerFactory(
            KafkaProperties kafkaProperties,
            @Value("${discodeit.kafka.realtime-push-group-id}") String groupId
    ) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> realtimePushKafkaListenerContainerFactory(
            ConsumerFactory<String, String> realtimePushConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(realtimePushConsumerFactory);
        return factory;
    }
}
