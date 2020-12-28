package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.TrainingModel;

import java.util.Set;

public interface TrainingModelService {

    TrainingModel getTrainingModelById(final Long id);

    Set<TrainingModel> findTrainingModelsForStockId(final Long stockId);

    TrainingModel saveTrainingModel(final TrainingModel trainingModel);

    void deleteTrainingModel(final Long id);
}
