package de.tse.predictivegrowth.service.impl;

import ai.djl.Model;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Block;
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
import de.tse.predictivegrowth.enumeration.TrainingStatus;
import de.tse.predictivegrowth.model.InOutData;
import de.tse.predictivegrowth.model.NormalizationData;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import de.tse.predictivegrowth.service.api.StockDataService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Optional;

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
    public void trainAndSaveMlpForModel(final Long modelId) {
        final TrainingModel trainingModel = this.trainingModelService.getTrainingModelById(modelId);
        final StockHistory stockHistory = this.stockDataService.getStockHistoryById(trainingModel.getHistoryId());
        final Pair<InOutData, NormalizationData> prepareReturn = this.stockDataPreparationService.fullyPrepare(stockHistory.getStockDayDataList(),
                trainingModel.getTrainingIntStart(), trainingModel.getTrainingIntEnd());

        // Prepare dataset
        final Dataset dataset = StockDataset.builder()
                .setSampling(16, true)
                .setData(prepareReturn.getValue0())
                .build();

        // Train DJL model
        final Model trainedDeepJavaModel = this.trainDeepJavaModel(trainingModel, dataset);
        final byte[] modelFile = this.getModelFileAsByteArray(trainingModel.getInstanceName(), trainedDeepJavaModel, stockHistory.getStockIdentifier());

        // Update trainingModel
        trainingModel.setStatus(TrainingStatus.SUCCESS);
        trainingModel.setModelFile(modelFile);
        trainingModel.setTrainingIntMin(prepareReturn.getValue1().getTrainingIntMin());
        trainingModel.setTrainingIntMax(prepareReturn.getValue1().getTrainingIntMax());
        this.trainingModelService.saveTrainingModel(trainingModel);
    }

    private Model trainDeepJavaModel(final TrainingModel trainingModel, final Dataset dataset) {
        final Model model = Model.newInstance(trainingModel.getInstanceName());
        model.setBlock(this.getMlpBlockForTrainingModel(trainingModel));
        final Trainer trainer = this.getConfiguredTrainer(model);

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

        model.setProperty("Epoch", String.valueOf(epoch));
        model.setProperty("Timestamp", ZonedDateTime.now().toString());
        return model;
    }

    private Block getMlpBlockForTrainingModel(final TrainingModel trainingModel) {
        // Ref.: The Application of Stock Index Price Prediction with Neural Network (file:///C:/Users/tse/Downloads/mca-25-00053.pdf)
        final SequentialBlock block = new SequentialBlock();
        block.add(Blocks.batchFlattenBlock(trainingModel.getInputLayer())); // Rule-of-thumb: half input of first layer

        for (int i = 0; i < trainingModel.getLayerUnits().size(); i++) {
            block.add(Linear.builder().setUnits(trainingModel.getLayerUnits().get(i)).build());
            block.add(Activation::relu);
        }
        block.add(Linear.builder().setUnits(trainingModel.getOutputLayer()).build());

        return block;
    }

    private byte[] getModelFileAsByteArray(final String instanceName, final Model trainingModel, final String stockIdentifier) {
        try {
            Path modelDir = Paths.get(MLP_DIRECTORY);

            Files.createDirectories(modelDir);
            trainingModel.setProperty("Stock", stockIdentifier);
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
