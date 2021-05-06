package ua.kovalev.recommendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserMappingService implements MappingService{

    @Autowired
    private JdbcTemplate template;

    @Value("${mapping.user-table:users}")
    private String userTable;

    @Override
    public Optional<Integer> getModelId(Integer outerId){
        Integer modelId = DataAccessUtils
                .singleResult(template.query("select model_id from " + userTable + " where outer_id = ? limit 1", (r, num) -> r.getInt(1), outerId));
        return Optional.ofNullable(modelId);
    }

    @Override
    public Optional<Integer> getOuterId(Integer modelId){
        Integer outerId = DataAccessUtils.singleResult(
                template.query("select outer_id from " + userTable + " where model_id = ? limit 1", (r, num) -> r.getInt(1), modelId)
        );
        return Optional.ofNullable(outerId);
    }

    @Override
    public boolean save(Integer outerId, Integer modelId){
        return template.update("insert into " + userTable + "(outer_id, model_id) values (?, ?)", outerId, modelId) == 1;
    }
}
