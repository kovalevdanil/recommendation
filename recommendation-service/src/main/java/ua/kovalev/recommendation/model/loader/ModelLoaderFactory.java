package ua.kovalev.recommendation.model.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ua.kovalev.recommendation.config.properties.ModelSources;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

@Component
public class ModelLoaderFactory {

    @Autowired
    @Qualifier("databaseModelLoader")
    ModelLoader databaseModelLoader;

    @Autowired
    @Qualifier("netflixModelLoader")
    ModelLoader netflixModelLoader;

    private Map<ModelSources, ModelLoader> modelLoaderMap;

    @PostConstruct
    public void init(){
        modelLoaderMap = new EnumMap<>(ModelSources.class);
        modelLoaderMap.put(ModelSources.DATABASE, databaseModelLoader);
        modelLoaderMap.put(ModelSources.NETFLIX, netflixModelLoader);
    }

    public ModelLoader getModelLoader(ModelSources source){
        return modelLoaderMap.get(source);
    }
}
