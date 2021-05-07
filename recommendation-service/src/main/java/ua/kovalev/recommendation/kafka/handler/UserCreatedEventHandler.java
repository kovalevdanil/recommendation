package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.event.data.UserCreatedData;
import ua.kovalev.recommendation.service.UserMappingService;

import java.util.Objects;

@Slf4j
public class UserCreatedEventHandler implements EventHandler{

    private final ObjectMapper mapper;
    private final EALSModel model;
    private final UserMappingService userMappingService;

    public UserCreatedEventHandler(ObjectMapper mapper, EALSModel model, UserMappingService userMappingService) {
        this.mapper = mapper;
        this.model = model;
        this.userMappingService = userMappingService;
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
        if (userMappingService.getModelId(outerId).isPresent()){
            log.info("Mapping for user {} already exists, skipping operation", outerId);
            return;
        }

        int modelId = model.addUser();
        boolean created = userMappingService.save(outerId, modelId);

        log.info("Mapping {}:{} was{}created", outerId, modelId, (created ? " " : " not "));
    }
}
