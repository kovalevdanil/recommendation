package ua.kovalev.recommendation.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;

public interface EventHandler {
    void handle(JsonNode data);
}
