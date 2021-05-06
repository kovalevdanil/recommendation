package ua.kovalev.recommendation.service;

import ua.kovalev.recommendation.config.properties.ModelProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;

import java.util.List;
import java.util.Map;

public interface ModelService {
    boolean updateUserVector(EALSModel model, int u);
    boolean updateItemVector(EALSModel model, int i);

    List<Integer> getRecommendations(EALSModel model, Integer u, boolean excludeInteracted);

    boolean saveUserVector(EALSModel model, int u);
    boolean saveItemVector(EALSModel model, int i);

    boolean persistUserInteraction(EALSModel model, int u, int i);
    void dumpModel(EALSModel model);

    EALSModel loadOnlyInteractions(Map<String, Object> config);
    EALSModel loadFullModel(Map<String, Object> config);
}
