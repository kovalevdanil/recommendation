package ua.kovalev.recommendation.service;

import java.util.Optional;

public interface MappingService {
    Optional<Integer> getModelId(Integer outerId);
    Optional<Integer> getOuterId(Integer modelId);
    boolean save(Integer outerId, Integer modelId);
}
