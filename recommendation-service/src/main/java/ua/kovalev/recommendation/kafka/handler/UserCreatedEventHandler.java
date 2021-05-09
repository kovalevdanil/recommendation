package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.model.event.data.UserCreatedData;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Objects;

@Slf4j
public class UserCreatedEventHandler implements EventHandler{

    private final ObjectMapper mapper;
    private final ModelService modelService;

    public UserCreatedEventHandler(ObjectMapper mapper, ModelService modelService) {
        this.mapper = mapper;
        this.modelService = modelService;
    }

    @Override
    public void handle(JsonNode data) {
        UserCreatedData userData;
        try {
            userData = mapper.convertValue(data, UserCreatedData.class);
        }  catch (Exception ex){
            log.info("Unable to parse '{}' into {}", data, UserCreatedData.class.getSimpleName());
            return;
        }
        log.info("UserCreatedData: {}", userData);

        Objects.requireNonNull(userData);
        Objects.requireNonNull(userData.getId());

        int outerId = userData.getId();
        modelService.addUser(outerId);
    }
}
