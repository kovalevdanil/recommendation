package ua.kovalev.recommendation.model.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public class UserRepository extends MappingRepository {

    @Value("${mapping.user-table:users}")
    private String userTable;

    @Override
    protected String getTableName() {
        return userTable;
    }
}
