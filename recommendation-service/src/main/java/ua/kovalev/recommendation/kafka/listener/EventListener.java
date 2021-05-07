package ua.kovalev.recommendation.kafka.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ua.kovalev.recommendation.kafka.config.KafkaProperties;
import ua.kovalev.recommendation.kafka.handler.EventHandler;
import ua.kovalev.recommendation.model.event.Event;
import ua.kovalev.recommendation.model.event.EventType;
import ua.kovalev.recommendation.model.event.Key;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Component
public class EventListener {

    private Map<EventType, EventHandler> eventTypeHandlerMap;

    @Autowired
    @Qualifier("userCreatedEventHandler")
    private EventHandler userCreatedEventHandler;

    @Autowired
    @Qualifier("itemCreatedEventHandler")
    private EventHandler itemCreatedEventHandler;

    @Autowired
    @Qualifier("interactionEventHandler")
    private EventHandler interactionEventHandler;

    @PostConstruct
    public void init(){
        EnumMap<EventType, EventHandler>  map = new EnumMap<>(EventType.class);

        map.put(EventType.USER_CREATED, userCreatedEventHandler);
        map.put(EventType.ITEM_CREATED, itemCreatedEventHandler);
        map.put(EventType.INTERACTION, interactionEventHandler);

        eventTypeHandlerMap = map;
    }

    @KafkaListener(containerFactory = "containerFactory", topics = "${kafka.topic.event-topic}")
    public void handleEvent(ConsumerRecord<Key, Event> record){
        log.info("New record: {}", record.toString());

        EventHandler handler = getHandler(record.value().getType());
        if (handler == null){
            log.info("No handler was found for event {}", record.value().getType());
            return;
        }

        handler.handle(record.value().getData());
    }

    private EventHandler getHandler(EventType type){
        return eventTypeHandlerMap.get(type);
    }
}
