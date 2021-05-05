package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.event.data.ItemCreatedData;
import ua.kovalev.recommendation.service.ItemMappingService;

import java.util.Objects;

@Slf4j
public class ItemCreatedEventHandler implements EventHandler{
    private final ObjectMapper mapper;
    private final EALSModel model;
    private final ItemMappingService itemMappingService;

    public ItemCreatedEventHandler(ObjectMapper mapper, EALSModel model, ItemMappingService itemMappingService) {
        this.mapper = mapper;
        this.model = model;
        this.itemMappingService = itemMappingService;
    }

    @Override
    public void handle(JsonNode data) {
        ItemCreatedData itemData = mapper.convertValue(data, ItemCreatedData.class);
        log.info("ItemCreatedData: {}", itemData);

        Objects.requireNonNull(itemData);
        Objects.requireNonNull(itemData.getId());

        int outerId = itemData.getId();
        int modelId = model.addItem();

        itemMappingService.save(outerId, modelId);
    }
}
