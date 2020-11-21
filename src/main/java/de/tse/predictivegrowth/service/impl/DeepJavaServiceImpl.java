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
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class DeepJavaServiceImpl implements DeepJavaService {

    private final StockDataPreparationService stockDataPreparationService;

    private Model createTrainingModel() {
        // Ref.: The Application of Stock Index Price Prediction with Neural Network (file:///C:/Users/tse/Downloads/mca-25-00053.pdf)
        final SequentialBlock block = new SequentialBlock();
        block.add(Blocks.batchFlattenBlock(50));
        block.add(Linear.builder().setUnits(70).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(28).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(14).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(7).build());
        block.add(Activation::relu);
        block.add(Linear.builder().setUnits(1).build());

        final Model model = Model.newInstance("mlp_stock");
        model.setBlock(block);
        return model;
    }

    private void trainModel() {
        final Model model = this.createTrainingModel();
        final Dataset dataset =
        final Trainer trainer = this.getConfiguredTrainer(model);

        // Deep learning is typically trained in epochs where each epoch trains the model on each item in the dataset once.
        int epoch = 2;
        for (int i = 0; i < epoch; ++i) {
            int index = 0;
            for (Batch batch : trainer.iterateDataset(mnist)) {
                EasyTrain.trainBatch(trainer, batch);
                trainer.step();
                batch.close();
            }
            // Call the end epoch event for the training listeners now that we are done
            trainer.notifyListeners(listener -> listener.onEpoch(trainer));
        }
    }

    private Trainer getConfiguredTrainer(final Model model) {
        // L1Loss ref.: https://afteracademy.com/blog/what-are-l1-and-l2-loss-functions
        // L2lossfunction according to paper cited above
        final DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss())
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .addTrainingListeners(TrainingListener.Defaults.logging());

        // Now that we have our training configuration, we should create a new trainer for our model
        Trainer trainer = model.newTrainer(config);
        trainer.initialize(new Shape(1, 1));
    }
}
