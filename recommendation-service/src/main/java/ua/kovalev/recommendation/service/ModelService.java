package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;

@Service
public class ModelService {

    private final JdbcTemplate template;

    @Value("model.initializer.tables.user-vector")
    private String userVectorTable;

    @Value("model.initializer.tables.item-vector")
    private String itemVectorTable;

    @Value("model.initializer.tables.user-interaction")
    private String userInteractionTable;

    @Autowired
    public ModelService(JdbcTemplate template, ModelInitializerProperties props) {
        this.template = template;
    }

}
