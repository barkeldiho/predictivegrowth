package de.tse.predictivegrowth.service.impl;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
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
import ai.djl.translate.NoopTranslator;
import ai.djl.translate.TranslateException;
import de.tse.predictivegrowth.dataset.StockDataset;
import de.tse.predictivegrowth.enumeration.TrainingStatus;
import de.tse.predictivegrowth.model.*;
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import de.tse.predictivegrowth.service.api.StockDataService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
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

    private final NDManager ndManager = NDManager.newBaseManager();

    @Override
    @Transactional
    public void trainAndSaveMlpForModel(final Long modelId) {
        final TrainingModel trainingModel = this.trainingModelService.getTrainingModelById(modelId);
        final StockHistory stockHistory = this.stockDataService.getStockHistoryById(trainingModel.getHistoryId());
        final Pair<InOutData, NormalizationData> prepareReturn = this.stockDataPreparationService.fullyPrepare(stockHistory.getStockDayDataList(),
                trainingModel.getInputLayer(),
                trainingModel.getTrainingIntStart(), trainingModel.getTrainingIntEnd());

        // Prepare dataset
        final Dataset dataset = StockDataset.builder()
                .setSampling(16, false)
                .setData(prepareReturn.getValue0())
                .build();

        // Train DJL model
        final Model trainedDeepJavaModel = this.trainDeepJavaModel(trainingModel, dataset, stockHistory.getStockIdentifier());
        final List<ModelFile> modelFiles = this.getModelFilesAsByteArray(trainingModel.getInstanceName(), trainedDeepJavaModel);
        trainedDeepJavaModel.close();

        // Update trainingModel
        trainingModel.setStatus(TrainingStatus.SUCCESS);
        trainingModel.setModelFiles(modelFiles);
        trainingModel.setTrainingIntMin(prepareReturn.getValue1().getTrainingIntMin());
        trainingModel.setTrainingIntMax(prepareReturn.getValue1().getTrainingIntMax());
        this.trainingModelService.saveTrainingModel(trainingModel);
    }

    @Override
    public List<Double> getRollingPredictionForModel(final Long modelId, final Integer outputCount) {
        final TrainingModel trainingModel = this.trainingModelService.getTrainingModelById(modelId);
        final StockHistory stockHistory = this.stockDataService.getStockHistoryById(trainingModel.getHistoryId());
        final List<StockDayData> stockDayDataList = stockHistory.getStockDayDataList();
        final Model predictionModel = this.loadPredictionModel(trainingModel);

        // Prepare prediction inputs
        final float[] predictionValues = new float[trainingModel.getInputLayer()];
        int index = stockDayDataList.size() - trainingModel.getInputLayer();
        for (int i = 0; i < trainingModel.getInputLayer(); i++) {
            predictionValues[i] = stockDayDataList.get(index).getPriceMean().floatValue();
            index++;
        }
        final NDArray predictionInputs = this.ndManager.create(predictionValues).reshape(new Shape(1, trainingModel.getInputLayer()));

        // Predict & Return
        final Predictor<NDList, NDList> predictor = predictionModel.newPredictor(new NoopTranslator());
        try {
            final List<Double> finalResults = new ArrayList<>();
            final NDList resultList = predictor.predict(new NDList(predictionInputs));
            for (int i = 0; i < trainingModel.getInputLayer(); i++) {
                final Double value = resultList.get(0).getDouble(i);
                finalResults.add(value);
            }
            predictionModel.close();
            return finalResults;
        } catch (TranslateException e) {
            throw new RuntimeException("Error during value prediction.");
        }
    }

    private Model loadPredictionModel(final TrainingModel trainingModel) {
        Model model = null;
        try {
            final String stringPath = MLP_DIRECTORY + "/" + RandomStringUtils.randomAlphanumeric(5) + "/";
            for (final ModelFile modelFile : trainingModel.getModelFiles()) {
                FileUtils.writeByteArrayToFile(new File(stringPath + trainingModel.getInstanceName() + modelFile.getFileEnding()),
                        modelFile.getFileData());
            }

            model = Model.newInstance(trainingModel.getInstanceName());
            final Path modelPath = Paths.get(stringPath);
            try {
                model.load(modelPath);
            } finally {
                for (final ModelFile modelFile : trainingModel.getModelFiles()) {
                    Files.delete(Paths.get(stringPath + trainingModel.getInstanceName() + modelFile.getFileEnding()));
                }
            }
        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException("Error while loading model from file.");
        }
        return model;
    }

    private Model trainDeepJavaModel(final TrainingModel trainingModel, final Dataset dataset, final String stockIdentifier) {
        final Model model = Model.newInstance(trainingModel.getInstanceName());
        model.setBlock(this.getMlpBlockForTrainingModel(trainingModel));
        final Trainer trainer = this.getConfiguredTrainer(model, trainingModel);

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
            // Call the end epoch event for the training listeners
            trainer.notifyListeners(listener -> listener.onEpoch(trainer));
        }

        model.setProperty("Stock", stockIdentifier);
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

    private List<ModelFile> getModelFilesAsByteArray(final String instanceName,final Model model) {
        try {
            final Path modelDir = Paths.get(MLP_DIRECTORY + "/" + RandomStringUtils.randomAlphanumeric(5));
            model.save(modelDir, instanceName);

            final List<Path> filePaths = Files.walk(modelDir)
                    .filter(entry -> entry.toString().contains(instanceName))
                    .collect(Collectors.toList());

            final List<ModelFile> modelFiles = new ArrayList<>();
            for (final Path filePath : filePaths) {
                final byte[] byteArray = Files.readAllBytes(filePath);
                modelFiles.add(ModelFile.builder()
                    .fileData(byteArray)
                    .fileEnding(FilenameUtils.EXTENSION_SEPARATOR + FilenameUtils.getExtension(filePath.getFileName().toString()))
                    .build());
                Files.delete(filePath);
            }
            return modelFiles;
        } catch (IOException e) {
            throw new RuntimeException("Problem during model save action.");
        }
    }

    private Trainer getConfiguredTrainer(final Model model, final TrainingModel trainingModel) {
        // L1Loss ref.: https://afteracademy.com/blog/what-are-l1-and-l2-loss-functions
        // L2lossfunction according to paper cited above
        final DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .addTrainingListeners(TrainingListener.Defaults.logging());

        Trainer trainer = model.newTrainer(config);
        trainer.initialize(new Shape(1, trainingModel.getInputLayer()));

        return trainer;
    }
}
