package de.tse.predictivegrowth.service;

import ai.djl.ndarray.NDManager;
import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import de.tse.predictivegrowth.service.api.StockDataService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import de.tse.predictivegrowth.service.impl.DeepJavaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeepJavaServiceImplTest {

    @Mock
    private StockDataPreparationService stockDataPreparationService;

    @Mock
    private TrainingModelService trainingModelService;

    @Mock
    private StockDataService stockDataService;

    @Mock
    private NDManager ndManager;

    private DeepJavaService deepJavaService;

    @BeforeEach
    public void initService() {
        this.deepJavaService = new DeepJavaServiceImpl(
                this.stockDataPreparationService,
                this.trainingModelService,
                this.stockDataService);
    }

    @Test
    public void testTrainAndSaveForModel() {
        // Arrange
        final Long modelId = 1L;
        final Long historyId = 2L;
        final Integer inputLayer = 1;
        final Integer trainingIntStart = 1;

        final TrainingModel trainingModel = new TrainingModel();
        trainingModel.setHistoryId(historyId);
        trainingModel.setInputLayer(inputLayer);
        trainingModel.setTrainingIntStart(trainingIntStart);

        Mockito.when(this.trainingModelService.getTrainingModelById(Mockito.anyLong())).thenReturn();

        // Act
        this.deepJavaService.trainAndSaveMlpForModel(modelId);

        // Assert
    }
}
