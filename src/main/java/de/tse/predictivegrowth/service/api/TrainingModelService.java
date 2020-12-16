package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.TrainingModel;

import java.util.Set;

public interface TrainingModelService {

    Set<TrainingModel> findTrainingModelsForStockId(final Long stockId);

    TrainingModel saveTrainingModel(final TrainingModel trainingModel);
}
