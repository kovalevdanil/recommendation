package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.event.data.UserCreatedData;

@Slf4j
public class UserCreatedEventHandler implements EventHandler{

    private final ObjectMapper mapper;
    private final EALSModel model;

    public UserCreatedEventHandler(ObjectMapper mapper, EALSModel model) {
        this.mapper = mapper;
        this.model = model;
    }

    @Override
    public void handle(JsonNode data) {
        UserCreatedData userData = mapper.convertValue(data, UserCreatedData.class);
        log.info("UserCreatedData: {}", userData);
    }
}
