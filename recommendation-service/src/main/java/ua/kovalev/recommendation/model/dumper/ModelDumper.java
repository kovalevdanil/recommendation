package ua.kovalev.recommendation.model.dumper;

import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;

public interface ModelDumper {
    void dump(EALSModel model);
}
