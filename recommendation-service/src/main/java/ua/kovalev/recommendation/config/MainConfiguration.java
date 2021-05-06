package ua.kovalev.recommendation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.kovalev.recommendation.kafka.handler.EventHandler;
import ua.kovalev.recommendation.kafka.handler.InteractionEventHandler;
import ua.kovalev.recommendation.kafka.handler.ItemCreatedEventHandler;
import ua.kovalev.recommendation.kafka.handler.UserCreatedEventHandler;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.service.ItemMappingService;
import ua.kovalev.recommendation.service.UserMappingService;

@Configuration
public class MainConfiguration {
    @Autowired
    ObjectMapper mapper;

    @Autowired
    EALSModel model;

    @Autowired
    UserMappingService userMappingService;

    @Autowired
    ItemMappingService itemMappingService;

    @Bean(name = "userCreatedEventHandler")
    public EventHandler userCreatedEventHandler(){
        return new UserCreatedEventHandler(mapper, model, userMappingService);
    }

    @Bean(name = "itemCreatedEventHandler")
    public EventHandler itemCreatedEventHandler(){
        return new ItemCreatedEventHandler(mapper, model, itemMappingService);
    }

    @Bean(name = "interactionEventHandler")
    public EventHandler interactionEventHandler(){
        return new InteractionEventHandler(mapper, model, userMappingService, itemMappingService);
    }

}
