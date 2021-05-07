package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemMappingService implements MappingService{

    @Autowired
    private JdbcTemplate template;

    @Value("${mapping.item-table:items}")
    private String itemTable;

    @Override
    public Optional<Integer> getModelId(Integer outerId){
        Integer modelId = DataAccessUtils
                .singleResult(template.query("select model_id from " + itemTable + " where outer_id = ? limit 1", (r, num) -> r.getInt(1), outerId));
        return Optional.ofNullable(modelId);
    }

    @Override
    public Optional<Integer> getOuterId(Integer modelId){
        Integer outerId = DataAccessUtils
                .singleResult(template.query("select outer_id from " + itemTable + " where model_id = ? limit 1", (r, num) -> r.getInt(1), modelId));
        return Optional.ofNullable(outerId);
    }

    @Override
    public boolean save(Integer outerId, Integer modelId){
        if (getModelId(outerId).isPresent()){
            return false;
        }
        return template.update("insert into " + itemTable + "(outer_id, model_id) values (?, ?)", outerId, modelId) == 1;
    }
}
