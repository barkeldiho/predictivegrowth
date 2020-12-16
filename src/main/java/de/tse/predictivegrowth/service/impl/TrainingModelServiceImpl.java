package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.dao.db.TrainingModelDao;
import de.tse.predictivegrowth.entity.db.TrainingModelEntity;
import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.StockDataService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class TrainingModelServiceImpl implements TrainingModelService {

    private final @NonNull TrainingModelDao trainingModelDao;

    @Override
    public Set<TrainingModel> findTrainingModelsForStockId(final Long stockId) {
        return this.trainingModelDao.findTrainingModelEntitiesByHistoryId(stockId).stream()
                .map(TrainingModel::new)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public TrainingModel saveTrainingModel(final TrainingModel trainingModel) {
        final TrainingModelEntity trainingModelEntity = this.trainingModelDao.save(new TrainingModelEntity(trainingModel));
        return new TrainingModel(trainingModelEntity);
    }
}
