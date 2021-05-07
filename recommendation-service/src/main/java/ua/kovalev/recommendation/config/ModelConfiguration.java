package ua.kovalev.recommendation.config;

import liquibase.pro.packaged.N;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.config.properties.ModelProperties;
import ua.kovalev.recommendation.config.properties.ModelSources;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.model.loader.DatabaseModelLoader;
import ua.kovalev.recommendation.model.loader.ModelLoader;
import ua.kovalev.recommendation.model.loader.NetflixModelLoader;
import ua.kovalev.recommendation.service.ModelService;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ModelConfiguration {

    @Autowired(required = false)
    ModelService modelService;

    @Autowired
    ModelInitializerProperties modelInitProps;

    @Autowired
    ModelProperties modelProps;

    @Bean(name = "modelConfig")
    public Map<String, Object> modelConfig(){

        Map<String, Object> config = new HashMap<>();

        config.put(EALSConfig.REGULARIZATION_PARAMETER, modelProps.getRegularizationParameter());
        config.put(EALSConfig.FACTORS, modelProps.getFactors());
        config.put(EALSConfig.OFFLINE_ITERATIONS, modelProps.getOfflineIterations());
        config.put(EALSConfig.ONLINE_ITERATIONS, modelProps.getOnlineIterations());
        config.put(EALSConfig.LATENT_INIT_DEVIATION, modelProps.getLatentInitDeviation());
        config.put(EALSConfig.LATENT_INIT_MEAN, modelProps.getLatentInitMean());
        config.put(EALSConfig.POPULARITY_SIGNIFICANCE, modelProps.getPopularitySignificance());
        config.put(EALSConfig.MISSING_DATA_WEIGHT, modelProps.getMissingDataWeight());
        config.put(EALSConfig.NEW_ITEM_WEIGHT, modelProps.getNewItemWeight());

        return config;
    }

    @Bean
    public EALSModel model(){
        ModelLoader loader = modelLoader();
        log.info("Using model loader {}", loader.getClass().getSimpleName());

        return loader.load();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void handleRefreshedContext(){
        if (modelInitProps.getTrain()){
            log.info("Model build");
            EALSModel model = model();
            model.buildModel();
        }
    }

    @Bean
    public ModelLoader modelLoader(){

        ModelSources source = modelInitProps.getSource();
        ModelLoader loader = null;

        if (ModelSources.NETFLIX.equals(source)){
            loader = new NetflixModelLoader(modelInitProps, modelConfig());
        } else if (ModelSources.DATABASE.equals(source)){
            loader = new DatabaseModelLoader(modelInitProps, modelConfig(), modelService);
        } else {
            throw new RuntimeException("it's not supposed to happen");
        }

        return loader;
    }


}
