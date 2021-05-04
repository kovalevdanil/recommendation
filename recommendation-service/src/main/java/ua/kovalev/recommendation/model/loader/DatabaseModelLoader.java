package ua.kovalev.recommendation.model.loader;

import org.springframework.jdbc.core.JdbcTemplate;
import ua.kovalev.recommendation.config.properties.ModelInitializerConstants;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.config.properties.ModelProperties;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.DenseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.util.DatasetUtils;
import ua.kovalev.recommendation.utils.SerializeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseModelLoader implements ModelLoader{

    private final JdbcTemplate jdbcTemplate;
    private final ModelInitializerProperties props;
    private final Map<String, Object> modelConfig;

    public DatabaseModelLoader(JdbcTemplate jdbcTemplate, ModelInitializerProperties props, Map<String, Object> modelConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.props = props;
        this.modelConfig = modelConfig;
    }


    @Override
    public EALSModel load() {
        EALSModel model = null;

        String userInteractionTable = props.getTables().getUserInteraction() == null ?
                ModelInitializerConstants.USER_INTERACTION_DEFAULT_TABLE_NAME  : props.getTables().getUserInteraction();

        String userVectorTable = props.getTables().getUserVector() == null ?
                ModelInitializerConstants.USER_VECTOR_DEFAULT_TABLE_NAME : props.getTables().getUserVector();

        String itemVectorTable = props.getTables().getItemVector() == null ?
                ModelInitializerConstants.ITEM_VECTOR_DEFAULT_TABLE_NAME : props.getTables().getItemVector();

        if (jdbcTemplate == null){
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

            model = new EALSModel(matrix, modelConfig);
        } else {
            int userCount = jdbcTemplate.queryForObject("select max(id) from " + userVectorTable, Integer.class) + 1;
            int itemCount = jdbcTemplate.queryForObject("select max(id) from " + itemVectorTable, Integer.class) + 1;

            Integer factors = (Integer) modelConfig.get(EALSConfig.FACTORS);
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

            model = new EALSModel(matrix, U, V, modelConfig);
        }

        return model;
    }
}
