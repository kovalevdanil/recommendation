package ua.kovalev.recommendation.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private Topic topic;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Topic {
        private String eventTopic;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String groupId;
        private String clientId;
        private String bootstrapServer;
        private Integer commitInterval;
        private Integer pollInterval;
        private Integer sessionTimeout;
    }
}
