package de.tse.predictivegrowth.dao;

import de.tse.predictivegrowth.entity.StockHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockHistoryDao extends JpaRepository<Integer, StockHistoryEntity> {
}
