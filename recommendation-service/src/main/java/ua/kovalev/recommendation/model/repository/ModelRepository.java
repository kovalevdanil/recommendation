package ua.kovalev.recommendation.model.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.DenseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.util.DatasetUtils;
import ua.kovalev.recommendation.mf.util.VectorUtils;
import ua.kovalev.recommendation.utils.SerializeUtils;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ModelRepository {
    @Autowired
    JdbcTemplate template;

    @Value("${model.initializer.tables.user-vector}")
    private String userVectorTable;

    @Value("${model.initializer.tables.item-vector}")
    private String itemVectorTable;

    @Value("${model.initializer.tables.user-interaction}")
    private String userInteractionTable;

    @Value("${mapping.user-table:users}")
    private String userTable;

    @Value("${mapping.item-table:items}")
    private String itemTable;

    @PostConstruct
    public void init(){
        // NOSONAR
    }

    // TODO update user and item vectors
    public boolean update(@NotNull EALSModel model, @NotNull Integer u, @NotNull Integer i){
        return template.update("insert into " + userInteractionTable + " values (?, ?)", u, i) > 0;
    }

    public boolean saveItem(@NotNull EALSModel model, @NotNull Integer i){
        return insertItemVector(i, model.getV().getRowRef(i));
    }

    public boolean saveUser(@NotNull EALSModel model, Integer u){
        return insertUserVector(u, model.getU().getRowRef(u));
    }

    public boolean updateUser(@NotNull EALSModel model, @NotNull Integer u){
        return updateUserVector(u, model.getU().getRowRef(u));
    }

    public boolean updateItem(@NotNull EALSModel model, @NotNull Integer i){
        return updateItemVector(i, model.getU().getRowRef(i));
    }

    /**
     * Load interaction matrix, user latent vectors and item latent vectors from database
     * @return model
     */
    public EALSModel loadTrained(Map<String, Object> config){
        assert config.containsKey(EALSConfig.FACTORS);

        Dataset dataset = loadDataset();
        SparseRealMatrix interactionMatrix = DatasetUtils.buildDatasetMatrix(dataset);

        int factors = (int) config.get(EALSConfig.FACTORS);
        DenseRealMatrix U = loadUserVectors(factors);
        DenseRealMatrix V = loadItemVectors(factors);

        assert U.getRowCount() == interactionMatrix.getRowCount();
        assert V.getRowCount() == interactionMatrix.getColumnCount();

        return new EALSModel(interactionMatrix, U, V, config);
    }

    /**
     * Load only interaction matrix
     * @return model
     */
    public EALSModel loadRaw(Map<String, Object> config){
        Dataset dataset = loadDataset();
        SparseRealMatrix interactionMatrix = DatasetUtils.buildDatasetMatrix(dataset);
        return new EALSModel(interactionMatrix, config);
    }

    public void save(EALSModel model){
        clearTable(userVectorTable);
        clearTable(itemVectorTable);
        clearTable(userInteractionTable);
        clearTable(itemTable);
        clearTable(userTable);

        saveInteractionMatrix(model.getTrainMatrix());
        saveUserVectors(model.getU());
        saveItemVectors(model.getV());
    }

    private void clearTable(String tableName){
        template.execute("delete from " + tableName);
    }

    private void saveInteractionMatrix(SparseRealMatrix matrix){
        for (int u = 0; u < matrix.getRowCount(); u++){
            for (int i : VectorUtils.getIndexList(matrix.getRowRef(u))){
                insertInteraction(u, i);
            }
        }
    }

    private void saveUserVectors(DenseRealMatrix U){
        for (int u = 0; u < U.getRowCount(); u++) {
            insertUserVector(u, U.getRowRef(u));
        }
    }

    private void saveItemVectors(DenseRealMatrix V){
        for (int i = 0; i < V.getRowCount(); i++) {
            insertItemVector(i, V.getRowRef(i));
        }
    }

    private boolean insertInteraction(int u, int i){
        return template.update("insert into " + userInteractionTable + " values (?, ?)", u, i) > 0;
    }

    private boolean insertUserVector(int u, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("insert into " + userVectorTable + " values(?, ?)", u, byteVector) > 0;
    }

    public boolean updateUserVector(Integer u, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("update " + userVectorTable + " set vector = ? where user_id = ?", byteVector, u) > 0;
    }

    private boolean insertItemVector(int i, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("insert into " + itemVectorTable + " values(?, ?)", i, byteVector) > 0;
    }

    public boolean updateItemVector(Integer i, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("update " + itemVectorTable + " set vector = ? where item_id = ?", byteVector, i) > 0;
    }

    private int getMaxUserId(){
        Integer maxUserTable = template.queryForObject(String.format("select max(model_id) from %s", userTable), Integer.class);
        Integer maxInteractionTable = template.queryForObject(String.format("select max(user_id) from %s", userInteractionTable), Integer.class);

        if (maxUserTable == null){
            return maxInteractionTable == null ? 0 : maxInteractionTable;
        }

        if (maxInteractionTable == null){
            return maxUserTable;
        }

        return Math.max(maxUserTable, maxInteractionTable);
    }

    private int getMaxItemId(){
        Integer maxItemTable = template.queryForObject(String.format("select max(model_id) from %s", itemTable), Integer.class);
        Integer maxInteractionTable = template.queryForObject(String.format("select max(item_id) from %s", userInteractionTable), Integer.class);

        if (maxItemTable == null){
            return maxInteractionTable == null ? 0 : maxInteractionTable;
        }

        if (maxInteractionTable == null){
            return maxItemTable;
        }

        return Math.max(maxItemTable, maxInteractionTable);
    }

    private Dataset loadDataset(){
        List<Rating> interactions = loadRatings();

        int userCount = getMaxUserId() + 1;
        int itemCount = getMaxItemId() + 1;

        return new Dataset(interactions, userCount, itemCount);
    }

    private List<Rating> loadRatings(){
        List<Rating> interactions = new ArrayList<>();

        template.query("select user_id, item_id from " + userInteractionTable, (rs) -> {
            interactions.add(new Rating(rs.getInt(1), rs.getInt(2)));
        });

        return interactions;
    }

    public DenseRealMatrix loadUserVectors(int factors){
        int userCount = getMaxUserId() + 1;

        DenseRealMatrix U = new DenseRealMatrix(userCount, factors);

        template.query("select user_id, vector from " + userVectorTable, (rs) -> {
            int u = rs.getInt(1);
            double[] vector = null;
            try {
                vector = SerializeUtils.deserializeByteArrayToDoubleArray(rs.getBytes(2));
            } catch (Exception ex){
                throw new RuntimeException();
            }
            U.setRow(u, new DenseRealVector(vector));
        });

        return U;
    }

    private DenseRealMatrix loadItemVectors(Integer factors) {
        int itemCount = getMaxItemId() + 1;

        DenseRealMatrix V = new DenseRealMatrix(itemCount, factors);

        template.query("select item_id, vector from " + itemVectorTable, (rs) -> {
            int u = rs.getInt(1);
            double[] vector = null;
            try {
                vector = SerializeUtils.deserializeByteArrayToDoubleArray(rs.getBytes(2));
            } catch (Exception ex){
                throw new RuntimeException();
            }
            V.setRow(u, new DenseRealVector(vector));
        });

        return V;
    }
}
