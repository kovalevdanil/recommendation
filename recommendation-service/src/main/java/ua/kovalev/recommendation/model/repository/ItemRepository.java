package ua.kovalev.recommendation.model.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ItemRepository {

    @Autowired
    JdbcTemplate template;

    @Value("${mapping.item-table:items}")
    private String itemTable;

    public boolean save(Integer outerId, Integer modelId) {
        if (exists(outerId, modelId)){
            return false;
        }
        return template.update("insert into " + itemTable + "(outer_id, model_id) values (?, ?)", outerId, modelId) == 1;
    }

    public Optional<Integer> findModelId(Integer outerId){
        Integer modelId = DataAccessUtils
                .singleResult(template.query("select model_id from " + itemTable + " where outer_id = ? limit 1", (r, num) -> r.getInt(1), outerId));
        return Optional.ofNullable(modelId);
    }

    public Optional<Integer> findOuterId(Integer modelId){
        Integer outerId = DataAccessUtils
                .singleResult(template.query("select outer_id from " + itemTable + " where model_id = ? limit 1", (r, num) -> r.getInt(1), modelId));
        return Optional.ofNullable(outerId);
    }

    public boolean exists(Integer outerId, Integer modelId){
        Integer rowCount = DataAccessUtils.singleResult(
                template.query("select count(*) from " + itemTable + " where outer_id = ? and model_id = ?",
                        (r, num) -> r.getInt(1),outerId, modelId)
        );

        return rowCount != null && rowCount > 0;
    }

    public boolean existsByOuterId(Integer outerId){
        Integer rowCount = DataAccessUtils.singleResult(
                template.query("select count(*) from " + itemTable + " where outer_id = ?",
                        (r, num) -> r.getInt(1),outerId)
        );

        return rowCount != null && rowCount > 0;
    }

    public boolean existsByModelId(Integer outerId){
        Integer rowCount = DataAccessUtils.singleResult(
                template.query("select count(*) from " + itemTable + " where model_id = ?",
                        (r, num) -> r.getInt(1),outerId)
        );

        return rowCount != null && rowCount > 0;
    }
}
