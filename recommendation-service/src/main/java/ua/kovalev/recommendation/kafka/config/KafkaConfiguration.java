package ua.kovalev.recommendation.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ua.kovalev.recommendation.kafka.error.SerializationErrorHandler;
import ua.kovalev.recommendation.model.event.Event;
import ua.kovalev.recommendation.model.event.Key;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfiguration {


    @Autowired
    KafkaProperties props;

    @Bean
    public NewTopic eventTopic(){
        return TopicBuilder
                .name(props.getTopic().getEventTopic())
                .build();
    }

    @Bean(name = "consumerConfig")
    public Map<String, Object> consumerFactoryConfig(){
        Map<String, Object> propsMap = new HashMap<>();

        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getConsumer().getBootstrapServer());
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, props.getConsumer().getGroupId());
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, props.getConsumer().getCommitInterval());
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, props.getConsumer().getSessionTimeout());
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, props.getConsumer().getPollInterval());
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return propsMap;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Key, Event> containerFactory(){
        ConcurrentKafkaListenerContainerFactory<Key, Event> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
                consumerFactoryConfig(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(Key.class)),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(Event.class))
        ));
        factory.setErrorHandler(new SerializationErrorHandler());

        return factory;
    }
}
