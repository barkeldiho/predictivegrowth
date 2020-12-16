package de.tse.predictivegrowth.service.impl;

import ai.djl.Model;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import de.tse.predictivegrowth.dataset.StockDataset;
import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import de.tse.predictivegrowth.service.api.StockDataService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class DeepJavaServiceImpl implements DeepJavaService {

    public static final String MLP_DIRECTORY = "resources/tmp/mlp";

    private final @NonNull StockDataPreparationService stockDataPreparationService;

    private final @NonNull TrainingModelService trainingModelService;

    private final @NonNull StockDataService stockDataService;

    @Override
    @Transactional
    public void trainAndSaveMlpForStockId(final String instanceName, final Long stockId) {
        final StockHistory stockHistory = this.stockDataService.getStockHistory(stockId);
        final List<StockDayData> preparedData = this.stockDataPreparationService.fullyPrepare(stockHistory.getStockDayDataList());

        final Model trainedDeepJavaModel = this.trainDeepJavaModel(instanceName, preparedData);
        final byte[] modelFile = this.getModelFileAsByteArray(instanceName, trainedDeepJavaModel, stockHistory);

        final TrainingModel trainingModel = TrainingModel.builder()
                .inputLayer(35)
                .layerUnits(new ArrayList<>(Arrays. asList(70, 28, 14, 7, 1)))
                .instanceName(instanceName)
                .historyId(stockId)
                .modelFile(modelFile)
                .status(2) // 1: IN PROGRESS, 2: SUCCESS, 3: FAILED
                .build();

        this.trainingModelService.saveTrainingModel(trainingModel);
    }

    private Model trainDeepJavaModel(final String instanceName, final List<StockDayData> stockDayDataList) {
        // Ref.: The Application of Stock Index Price Prediction with Neural Network (file:///C:/Users/tse/Downloads/mca-25-00053.pdf)
        final SequentialBlock block = new SequentialBlock();
        block.add(Blocks.batchFlattenBlock(1)); // Rule-of-thumb: half input of first layer
        block.add(Linear.builder().setUnits(70).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(28).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(14).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(7).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(1).build());

        final Model trainingModel = Model.newInstance(instanceName);
        trainingModel.setBlock(block);

        final Dataset dataset = StockDataset.builder()
                .setSampling(35, false)
                .setData(stockDayDataList)
                .build();
        final Trainer trainer = this.getConfiguredTrainer(trainingModel);

        // Deep learning is typically trained in epochs where each epoch trains the model on each item in the dataset once.
        int epoch = 5;
        for (int i = 0; i < epoch; ++i) {
            try {
                for (Batch batch : trainer.iterateDataset(dataset)) {
                    EasyTrain.trainBatch(trainer, batch);
                    trainer.step();
                    batch.close();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error during model training in epoch.");
            }
            // Call the end epoch event for the training listeners now that we are done
            trainer.notifyListeners(listener -> listener.onEpoch(trainer));
        }

        trainingModel.setProperty("Epoch", String.valueOf(epoch));
        return trainingModel;
    }

    private byte[] getModelFileAsByteArray(final String instanceName, final Model trainingModel, final StockHistory stockHistory) {
        try {
            Path modelDir = Paths.get(MLP_DIRECTORY);

            Files.createDirectories(modelDir);
            trainingModel.setProperty("Stock", stockHistory.getStockIdentifier());
            trainingModel.save(modelDir, instanceName);

            final Optional<Path> filePath = Files.walk(Paths.get(MLP_DIRECTORY))
                    .filter(entry -> entry.toString().contains(instanceName))
                    .findFirst();

            return Files.readAllBytes(filePath.orElseThrow(() -> new IOException("No such file.")));
        } catch (IOException e) {
            throw new RuntimeException("Problem during model save action.");
        }
    }

    private Trainer getConfiguredTrainer(final Model model) {
        // L1Loss ref.: https://afteracademy.com/blog/what-are-l1-and-l2-loss-functions
        // L2lossfunction according to paper cited above
        final DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .addTrainingListeners(TrainingListener.Defaults.logging());

        Trainer trainer = model.newTrainer(config);
        trainer.initialize(new Shape(1, 35));

        return trainer;
    }
}
