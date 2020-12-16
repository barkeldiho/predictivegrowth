package de.tse.predictivegrowth.dao.db;

import de.tse.predictivegrowth.entity.db.TrainingModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TrainingModelDao extends JpaRepository<TrainingModelEntity, Long> {

    Set<TrainingModelEntity> findTrainingModelEntitiesByHistoryId(final Long stockId);
}
