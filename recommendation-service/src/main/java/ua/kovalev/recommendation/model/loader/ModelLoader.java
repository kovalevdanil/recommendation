package ua.kovalev.recommendation.model.loader;

import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;

import java.util.Map;

public interface ModelLoader {
    EALSModel load(Map<String, Object> config);
}
