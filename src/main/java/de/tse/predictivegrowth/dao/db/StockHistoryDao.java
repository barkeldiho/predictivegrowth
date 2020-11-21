package de.tse.predictivegrowth.dao.db;

import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockHistoryDao extends JpaRepository<StockHistoryEntity, Long> {

    Optional<StockHistoryEntity> findByStockIdentifier(final String stockIdentifier);
}
