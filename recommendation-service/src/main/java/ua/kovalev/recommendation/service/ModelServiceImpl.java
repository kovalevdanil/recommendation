package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
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

import java.util.*;

import static ua.kovalev.recommendation.utils.AssertUtils.requireTrue;

@Service
public class ModelServiceImpl implements ModelService {

    private final JdbcTemplate template;

    @Value("${model.initializer.tables.user-vector}")
    private String userVectorTable;

    @Value("${model.initializer.tables.item-vector}")
    private String itemVectorTable;

    @Value("${model.initializer.tables.user-interaction}")
    private String userInteractionTable;

    @Autowired
    public ModelServiceImpl(JdbcTemplate template, ModelInitializerProperties props) {
        this.template = template;

    }

    @Override
    public boolean updateUserVector(EALSModel model, int u) {
        double[] vector = model.getU().getRowRef(u);
        return updateUserVector(u, vector);
    }

    @Override
    public boolean updateItemVector(EALSModel model, int i) {
        double[] vector = model.getV().getRowRef(i);
        return updateItemVector(i, vector);
    }

    @Override
    public boolean persistUserInteraction(EALSModel model, int u, int i) {
        requireTrue(u >= 0 && u < model.getTrainMatrix().getRowCount(), "User ID out of bounds [0, " + model.getTrainMatrix().getRowCount() + "]");
        requireTrue(i >= 0 && i < model.getTrainMatrix().getColumnCount(), "Item ID out of bounds [0, " + model.getTrainMatrix().getColumnCount() + "]");
        requireTrue(model.getTrainMatrix().getEntry(u, i) == 0, "Interaction is already captured");

        return saveInteraction(u, i) && updateUserVector(model, u) && updateItemVector(model, i);
    }

    /**
     * Method will rewrite whole database (it will drop existing vectors and interactions)
     * @param model model to dump
     */
    @Override
    public void dumpModel(EALSModel model) {
        clearTable(userVectorTable);
        clearTable(itemVectorTable);
        clearTable(userInteractionTable);

        saveInteractionMatrix(model.getTrainMatrix());
        saveUserVectors(model.getU());
        saveItemVectors(model.getV());
    }

    @Override
    public EALSModel loadOnlyInteractions(Map<String, Object> config) {
        Dataset dataset = loadDataset();
        SparseRealMatrix interactionMatrix = DatasetUtils.buildDatasetMatrix(dataset);
        return new EALSModel(interactionMatrix, config);
    }

    @Override
    public EALSModel loadFullModel(Map<String, Object> config) {
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

    private void saveInteractionMatrix(SparseRealMatrix matrix){
        for (int u = 0; u < matrix.getRowCount(); u++){
            for (int i : VectorUtils.getIndexList(matrix.getRowRef(u))){
                saveInteraction(u, i);
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

    private void clearTable(String tableName){
        template.execute("delete from " + tableName);
    }

    public int getMaxUserId(){
        Integer max = template.queryForObject(String.format("select max(user_id) from %s", userVectorTable), Integer.class);
        return (max == null ? 0 : max);
    }

    public int getMaxItemId(){
        Integer max =  template.queryForObject(String.format("select max(item_id) from %s", itemVectorTable), Integer.class);
        return (max == null ? 0 : max);
    }

    public boolean saveInteraction(Integer u, Integer i){
        return template.update("insert into " + userInteractionTable + " values (?, ?)", u, i) > 0;
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

    public boolean insertUserVector(Integer u, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("insert into " + userVectorTable + " values(?, ?)", u, byteVector) > 0;
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

    public boolean insertItemVector(Integer i, double[] vector){
        byte[] byteVector = null;
        try {
            byteVector = SerializeUtils.serializeDoubleArray(vector);
        } catch (Exception ex){
            throw new RuntimeException("Unable to serialize vector");
        }

        return template.update("insert into " + itemVectorTable + " values(?, ?)", i, byteVector) > 0;
    }

    public List<Rating> loadRatings(){
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

    private Dataset loadDataset() {
        List<Rating> interactions = loadRatings();

        int userCount = interactions.stream()
                .max(Comparator.comparing(Rating::getUserId))
                .orElse(new Rating(0, 0))
                .getUserId() + 1;

        int itemCount = interactions.stream()
                .max(Comparator.comparing(Rating::getItemId))
                .orElse(new Rating(0, 0))
                .getItemId() + 1;

        return new Dataset(interactions, userCount, itemCount);
    }
}
