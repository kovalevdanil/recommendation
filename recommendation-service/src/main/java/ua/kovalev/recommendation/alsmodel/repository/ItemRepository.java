package ua.kovalev.recommendation.alsmodel.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public class ItemRepository extends MappingRepository {
    @Value("${mapping.item-table:items}")
    private String itemTable;

    @Override
    protected String getTableName() {
        return itemTable;
    }
}
