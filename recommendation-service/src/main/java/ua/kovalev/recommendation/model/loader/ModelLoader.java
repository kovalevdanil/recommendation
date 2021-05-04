package ua.kovalev.recommendation.model.loader;

import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;

public interface ModelLoader {
    EALSModel load();
}
