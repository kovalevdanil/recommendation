package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ua.kovalev.recommendation.config.properties.ModelInitializerProperties;
import ua.kovalev.recommendation.utils.SerializeUtils;

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

    public int getMaxUserId(){
        return template.queryForObject(String.format("select max(user_id) from %s", itemVectorTable), Integer.class);
    }

    public int getMaxItemId(){
        return template.queryForObject(String.format("select max(item_id) from %s", itemVectorTable), Integer.class);
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
}
