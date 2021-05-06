package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.event.data.InteractionData;
import ua.kovalev.recommendation.service.ItemMappingService;
import ua.kovalev.recommendation.service.UserMappingService;

import java.util.Objects;

@Slf4j
public class InteractionEventHandler implements EventHandler{

    private final ObjectMapper mapper;
    private final EALSModel model;
    private final UserMappingService userMappingService;
    private final ItemMappingService itemMappingService;

    public InteractionEventHandler(ObjectMapper mapper, EALSModel model, UserMappingService userMappingService, ItemMappingService itemMappingService) {
        this.mapper = mapper;
        this.model = model;
        this.userMappingService = userMappingService;
        this.itemMappingService = itemMappingService;
    }

    @Override
    public void handle(JsonNode data) {
        InteractionData interactionData = mapper.convertValue(data, InteractionData.class);
        log.info("InteractionData: {}", interactionData);

        Objects.requireNonNull(interactionData);
        Objects.requireNonNull(interactionData.getItemId());
        Objects.requireNonNull(interactionData.getUserId());

        Integer outerUserId = interactionData.getUserId();
        Integer modelUserId = userMappingService.getModelId(outerUserId).orElse(outerUserId);

        Integer outerItemId = interactionData.getItemId();
        Integer modelItemId = itemMappingService.getModelId(outerItemId).orElse(outerItemId);

        model.updateModel(modelUserId, modelItemId);
    }
}
