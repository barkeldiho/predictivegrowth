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
import de.tse.predictivegrowth.util.DataProcessUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        trainingModel.setTrainingIntMin(prepareReturn.getValue1().getTrainingIntMin());
        trainingModel.setTrainingIntMax(prepareReturn.getValue1().getTrainingIntMax());

        // Prepare dataset
        final Dataset dataset = StockDataset.builder()
                .setSampling(16, false)
                .setData(prepareReturn.getValue0())
                .build();

        // Train DJL model
        final Model trainedDeepJavaModel = this.trainDeepJavaModel(trainingModel, dataset, stockHistory.getStockIdentifier());
        final List<ModelFile> modelFiles = this.getModelFilesAsByteArray(trainingModel.getInstanceName(), trainedDeepJavaModel);
        final Quartet<Double, Double, Double, Integer> accuracyMetrics = this.getAccuracyMetrics(trainingModel, trainedDeepJavaModel, stockHistory.getStockDayDataList());

        trainedDeepJavaModel.close();

        // Update trainingModel
        trainingModel.setStatus(TrainingStatus.SUCCESS);
        trainingModel.setModelFiles(modelFiles);
        trainingModel.setR2(accuracyMetrics.getValue0());
        trainingModel.setMae(accuracyMetrics.getValue1());
        trainingModel.setRmse(accuracyMetrics.getValue2());
        trainingModel.setTlccMaxLag(accuracyMetrics.getValue3());

        this.trainingModelService.saveTrainingModel(trainingModel);
    }

    @Override
    @Transactional
    // Recursive multi-step forecasting vs direct multi-step forecasting -> direct recursive hybrid strategy
    public List<Double> getRollingPredictionForModel(final Long modelId, final Integer outputCount, final int start) {
        final TrainingModel trainingModel = this.trainingModelService.getTrainingModelById(modelId);
        final StockHistory stockHistory = this.stockDataService.getStockHistoryById(trainingModel.getHistoryId());
        final List<StockDayData> stockDayDataList = stockHistory.getStockDayDataList();
        final Model predictionModel = this.loadPredictionModel(trainingModel);

        // Prepare initial prediction inputs
        float[] predictionValues = this.getNormalizedPredictionValueArray(stockDayDataList, trainingModel, start-trainingModel.getInputLayer());

        final List<Double> resultList = new ArrayList<>();
        for (int i = 0; i < outputCount; i++) {
            final Double predictedValue = this.getPredictionValueForInputs(predictionModel, predictionValues);
            final Double denormalizedPredictedValue = DataProcessUtil.getDenormalizedValueForMinMax(predictedValue, trainingModel.getTrainingIntMax(), trainingModel.getTrainingIntMin());
            resultList.add(denormalizedPredictedValue);
            predictionValues = DataProcessUtil.shiftArrayContentLeft(predictionValues);
            predictionValues[trainingModel.getInputLayer()-1] = predictedValue.floatValue();
        }
        predictionModel.close();
        return resultList;
    }

    private Quartet<Double, Double, Double, Integer> getAccuracyMetrics(final TrainingModel trainingModel, final Model predictionModel, final List<StockDayData> stockDayDataList) {
        final List<Double> actual = new ArrayList<>();
        final List<Double> predicted = new ArrayList<>();

        // Prepare actual and predicted data
        for (Long i = trainingModel.getVerificationIntStart(); i < trainingModel.getVerificationIntEnd(); i++) {
                final float[] predictionValues = this.getNormalizedPredictionValueArray(stockDayDataList, trainingModel,
                        (i.intValue() - trainingModel.getInputLayer()));
                final Double predictedValue = DataProcessUtil.getDenormalizedValueForMinMax(
                        this.getPredictionValueForInputs(predictionModel, predictionValues), trainingModel.getTrainingIntMax(), trainingModel.getTrainingIntMin()
                );
                final Double actualValue = stockDayDataList.get(i.intValue()).getPriceMean();
                actual.add(actualValue);
                predicted.add(predictedValue);
        }
        final Double rSquared = DataProcessUtil.calcRSquared(actual, predicted);
        final Double mae = DataProcessUtil.calcMeanAbsoluteError(actual, predicted);
        final Double rmse = DataProcessUtil.calcRootMeanSquaredError(actual, predicted);
        final List<Double> tlcc = DataProcessUtil.calcTlcc(actual, predicted);
        // https://stackoverflow.com/a/55120938/10114822
        final int timeLagWithMaxC = IntStream.range(0, tlcc.size()).boxed().max(Comparator.comparing(tlcc::get)).orElse(-1);
        return new Quartet<>(rSquared, mae, rmse, timeLagWithMaxC);
    }

    private float[] getNormalizedPredictionValueArray(final List<StockDayData> stockDayDataList, final TrainingModel trainingModel, final int start) {
        @SuppressWarnings("UnnecessaryUnboxing")
        float[] predictionValues = new float[trainingModel.getInputLayer().intValue()];
        int index = start;
        for (int i = 0; i < trainingModel.getInputLayer(); i++) {
            predictionValues[i] = DataProcessUtil.getNormalizedValueForMinMax(stockDayDataList.get(index).getPriceMean(),
                    trainingModel.getTrainingIntMax(), trainingModel.getTrainingIntMin()).floatValue();
            index++;
        }
        return predictionValues;
    }

    private Double getPredictionValueForInputs(final Model predictionModel, final float[] predictionValues) {
        final NDArray predictionInputs = this.ndManager.create(predictionValues).reshape(new Shape(1, predictionValues.length));

        // Predict & Return
        final Predictor<NDList, NDList> predictor = predictionModel.newPredictor(new NoopTranslator());
        try {
            final NDList resultList = predictor.predict(new NDList(predictionInputs));
            return (Double) (double) resultList.get(0).getFloat(0);
        } catch (TranslateException e) {
            throw new RuntimeException("Error during value prediction.");
        }
    }

    private Model loadPredictionModel(final TrainingModel trainingModel) {
        Model model = null;
        try {
            final String stringPath = MLP_DIRECTORY + "/" + RandomStringUtils.randomAlphanumeric(5);
            final String fileName = trainingModel.getInstanceName() + "-" +
                    StringUtils.leftPad(trainingModel.getEpochs().toString(), 4, "0");

            for (final ModelFile modelFile : trainingModel.getModelFiles()) {
                FileUtils.writeByteArrayToFile(new File(stringPath + "/" + fileName + modelFile.getFileEnding()),
                        modelFile.getFileData());
            }

            model = Model.newInstance(trainingModel.getInstanceName());
            model.setBlock(this.getMlpBlockForTrainingModel(trainingModel));
            final Path modelPath = Paths.get(stringPath);
            try {
                model.load(modelPath);
            } finally {
                for (final ModelFile modelFile : trainingModel.getModelFiles()) {
                    Files.delete(Paths.get(stringPath + "/" + fileName + modelFile.getFileEnding()));
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
        int epoch = trainingModel.getEpochs();
        for (int i = 0; i < epoch; ++i) {
            try {
                for (Batch batch : trainer.iterateDataset(dataset)) {
                    EasyTrain.trainBatch(trainer, batch);
                    trainer.step();
                    // LOGGING
                    trainer.getTrainingResult().getValidateEvaluation("Accuracy");

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
