package ua.kovalev.recommendation.model.loader;

import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.service.ModelService;

import java.util.Map;

public class DatabaseModelLoader implements ModelLoader{

    private final ModelService modelService;
    private final Map<String, Object> config;
    private final ModelInitializerProperties initializerProperties;

    public DatabaseModelLoader(ModelInitializerProperties props, Map<String, Object> config, ModelService modelService) {
        this.modelService = modelService;
        this.config = config;
        this.initializerProperties = props;
    }

    @Override
    public EALSModel load() {
        EALSModel model = null;

        if (initializerProperties.getTrain()){
            model = modelService.loadOnlyInteractions(config);
        } else {
            model = modelService.loadFullModel(config);
        }

        return model;
    }
}
