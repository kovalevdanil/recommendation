package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.model.event.data.ItemCreatedData;

@Slf4j
public class ItemCreatedEventHandler implements EventHandler{
    private final ObjectMapper mapper;
    private final EALSModel model;

    public ItemCreatedEventHandler(ObjectMapper mapper, EALSModel model) {
        this.mapper = mapper;
        this.model = model;
    }


    @Override
    public void handle(JsonNode data) {
        ItemCreatedData itemData = mapper.convertValue(data, ItemCreatedData.class);
        log.info("ItemCreatedData: {}", itemData);
    }
}
