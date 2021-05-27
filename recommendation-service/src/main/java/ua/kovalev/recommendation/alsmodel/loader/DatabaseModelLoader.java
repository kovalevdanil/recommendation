package ua.kovalev.recommendation.alsmodel.loader;

import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.alsmodel.repository.ModelRepository;

import java.util.Map;

public class DatabaseModelLoader implements ModelLoader{

    private final ModelInitializerProperties initializerProperties;
    private final ModelRepository modelRepository;

    public DatabaseModelLoader(ModelInitializerProperties props, ModelRepository modelRepository) {
        this.initializerProperties = props;
        this.modelRepository = modelRepository;
    }

    @Override
    public EALSModel load(Map<String, Object> config) {
        EALSModel model = null;

        if (initializerProperties.getTrain()){
            model = modelRepository.loadRaw(config);
        } else {
            model = modelRepository.loadTrained(config);
        }

        return model;
    }
}
