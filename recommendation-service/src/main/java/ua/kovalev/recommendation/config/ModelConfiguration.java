package ua.kovalev.recommendation.config;

import liquibase.pro.packaged.D;
import liquibase.pro.packaged.E;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ua.kovalev.recommendation.config.properties.ModelInitializerConstants;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.config.properties.ModelProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.DatasetConstants;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.DenseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.filter.ActiveUsersDatasetFilter;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;
import ua.kovalev.recommendation.mf.filter.ShrinkUsersDatasetFilter;
import ua.kovalev.recommendation.mf.reader.NetflixRatingReader;
import ua.kovalev.recommendation.mf.util.DatasetUtils;
import ua.kovalev.recommendation.utils.SerializeUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class ModelConfiguration {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    public EALSModel model(ModelInitializerProperties props, DataSource dataSource){

        log.info("Initializing model...");

        log.info("Initialized datasource: " + dataSource.toString());

        // if it's specified to train, then only interactionMatrix is loaded, otherwise
        // latent vectors are loaded if exist

        // config should have:
        // 1. user interaction table name
        // 2. user latent vector table name
        // 3. items latent vector table name

        // what should happen
        // 1. check if tables exist, otherwise throw exception
        // 2. check if training is needed (latent vector tables are not specified)
        // 2.1 if training is needed, load only interaction matrix
        // 2.2 otherwise load vectors U, V
        // 3. construct model based on existing data

        EALSModel model = null;
        if (props.getLoadNetflix() && props.getTrain()){
            log.info("Loading netflix dataset...");

            List<DatasetFilter> filters = new ArrayList<>();

            if (props.getUserInteractionThreshold() != null){
                filters.add(new ActiveUsersDatasetFilter(props.getUserInteractionThreshold()));
            }

            if (props.getShrinkUsers()){
                filters.add(new ShrinkUsersDatasetFilter());
            }

            NetflixRatingReader reader = new NetflixRatingReader(DatasetConstants.NETLFIX_DATASET, filters);

            Dataset dataset = null;

            try {
                dataset = reader.read(props.getLoadItemCount());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to load the model");
            }

            SparseRealMatrix matrix = DatasetUtils.buildDatasetMatrix(dataset);

            model = new EALSModel(matrix, modelConfig());
        } else {

            log.info("Loading dataset from database...");

            String userInteractionTable = props.getTables().getUserInteraction() == null ?
                    ModelInitializerConstants.USER_INTERACTION_DEFAULT_TABLE_NAME  : props.getTables().getUserInteraction();

            String userVectorTable = props.getTables().getUserVector() == null ?
                    ModelInitializerConstants.USER_VECTOR_DEFAULT_TABLE_NAME : props.getTables().getUserVector();

            String itemVectorTable = props.getTables().getItemVector() == null ?
                    ModelInitializerConstants.ITEM_VECTOR_DEFAULT_TABLE_NAME : props.getTables().getItemVector();

            if (jdbcTemplate == null){
                log.error("JdbcTemplate is no initialized");
                throw new RuntimeException("JdbcTemplate is not configured");
            }

            if (props.getTrain()){
                int userCount = jdbcTemplate
                        .queryForObject("select max(user_id) from " + userInteractionTable, Integer.class) + 1;

                int itemCount = jdbcTemplate
                        .queryForObject("select max(item_id) from " + userInteractionTable, Integer.class) + 1;

                List<Rating> interactions = new ArrayList<>();

                jdbcTemplate.query("select user_id, item_id from " + userInteractionTable, (rs) -> { // rs - result set
                    interactions.add(new Rating(rs.getInt(1), rs.getInt(2)))   ;
                });

                Dataset dataset = new Dataset(interactions, userCount, itemCount);

                SparseRealMatrix matrix = DatasetUtils.buildDatasetMatrix(dataset);

                model = new EALSModel(matrix, modelConfig());
            } else {
                int userCount = jdbcTemplate.queryForObject("select max(id) from " + userVectorTable, Integer.class) + 1;
                int itemCount = jdbcTemplate.queryForObject("select max(id) from " + itemVectorTable, Integer.class) + 1;

                Integer factors = modelProps.getFactors();
                if (factors == null) {
                    throw new RuntimeException("Property 'factors' must be specified");
                }

                DenseRealMatrix U = new DenseRealMatrix(userCount, factors);
                DenseRealMatrix V = new DenseRealMatrix(itemCount, factors);

                jdbcTemplate.query("select user_id, vector from " + userVectorTable, (rs) -> {
                    int userId = rs.getInt(1);
                    byte[] byteVector = rs.getBytes(1);

                    double[] vector = null;
                    try {
                        vector = SerializeUtils.deserializeByteArrayToDoubleArray(byteVector);
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }

                    U.setRow(userId, new DenseRealVector(vector));
                });

                jdbcTemplate.query("select item_id, vector from " + itemVectorTable, (rs) -> {
                    int itemId = rs.getInt(1);
                    byte[] byteVector = rs.getBytes(1);

                    double[] vector = null;
                    try {
                        vector = SerializeUtils.deserializeByteArrayToDoubleArray(byteVector);
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }

                    V.setRow(itemId, new DenseRealVector(vector));
                });

                List<Rating> interactions = new ArrayList<>();
                jdbcTemplate.query("select user_id, item_id from " + userInteractionTable, (rs) -> { // rs - result set
                    interactions.add(new Rating(rs.getInt(1), rs.getInt(2)))   ;
                });

                Dataset dataset = new Dataset(interactions, userCount, itemCount);

                SparseRealMatrix matrix = DatasetUtils.buildDatasetMatrix(dataset);

                model = new EALSModel(matrix, U, V, modelConfig());
            }
        }

        return model;
    }

    @Bean(name = "modelLoaderConfig")
    public Map<String, Object> modelLoaderConfig(){
        return null;
    }


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
        config.put(EALSConfig.NEW_ITEM_WEIGHT, modelProps.getNewItemWeight());
        config.put(EALSConfig.THREAD_NUMBER, modelProps.getThreadNumber());
        config.put(EALSConfig.TOP_K, modelProps.getTopK());

        return config;
    }

}
