package de.tse.predictivegrowth.controller;

import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(TrainingModelController.TRAINING_MODELS_PATH)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "DeepJava", description = "Endpoint to control deep learning models and analysis.")
public class TrainingModelController {

    public static final String TRAINING_MODELS_PATH = "/v1/training-models";

    private final TrainingModelService trainingModelService;

    @Operation(summary = "Method saves a training model for a stock history.",
            description = "Method saves a training model for a stock history.")
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public void saveTrainingModel(@Valid final TrainingModel trainingModel) {
        this.trainingModelService.saveTrainingModel(trainingModel);
    }

    @Operation(summary = "Method finds all training models for a stock history id.",
            description = "Method finds all training models for a stock history id.")
    @GetMapping(path = "/{stockId}")
    public Set<TrainingModel> findTraingingModelsForStockId(@PathVariable final Long stockId) {
        return this.trainingModelService.findTrainingModelsForStockId(stockId);
    }

    @Operation(summary = "Method deletes a training model with a given technical id.",
            description = "Method deletes a training model with a given technical id.")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus( code = HttpStatus.NO_CONTENT)
    public void deleteTrainingModel(@PathVariable final Long id) {
        this.trainingModelService.deleteTrainingModel(id);
    }
}
