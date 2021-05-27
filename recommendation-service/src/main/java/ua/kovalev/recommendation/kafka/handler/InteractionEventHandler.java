package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.model.event.data.InteractionData;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Objects;

@Slf4j
public class InteractionEventHandler implements EventHandler{

    private final ObjectMapper mapper;
    private final ModelService modelService;

    public InteractionEventHandler(ObjectMapper mapper, ModelService modelService) {
        this.mapper = mapper;
        this.modelService = modelService;
    }

    @Override
    public void handle(JsonNode data) {
        InteractionData interactionData = null;
        try{
            interactionData = mapper.convertValue(data, InteractionData.class);
        } catch (Exception ex){
            log.error("Unable to parse '{}' into {}", data, InteractionData.class.getSimpleName());
            return;
        }
        log.info("InteractionData: {}", interactionData);

        Objects.requireNonNull(interactionData);
        Objects.requireNonNull(interactionData.getItemId());
        Objects.requireNonNull(interactionData.getUserId());

        modelService.update(interactionData.getUserId(), interactionData.getItemId());
    }
}
