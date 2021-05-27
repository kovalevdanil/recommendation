package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.model.event.data.ItemCreatedData;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Objects;

@Slf4j
public class ItemCreatedEventHandler implements EventHandler{
    private final ObjectMapper mapper;
    private final ModelService modelService;

    public ItemCreatedEventHandler(ObjectMapper mapper, ModelService modelService) {
        this.mapper = mapper;
        this.modelService = modelService;
    }

    @Override
    public void handle(JsonNode data) {
        ItemCreatedData itemData = null;
        try {
            itemData = mapper.convertValue(data, ItemCreatedData.class);
        }  catch (Exception ex){
            log.info("Unable to parse '{}' into {}", data, ItemCreatedData.class.getSimpleName());
            return;
        }
        log.info("ItemCreatedData: {}", itemData);

        Objects.requireNonNull(itemData);
        Objects.requireNonNull(itemData.getId());

        int outerId = itemData.getId();
        modelService.addItem(outerId);
    }
}
