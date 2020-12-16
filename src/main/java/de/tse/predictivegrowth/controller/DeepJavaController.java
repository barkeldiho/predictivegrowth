package de.tse.predictivegrowth.controller;

import de.tse.predictivegrowth.model.TrainingModel;
import de.tse.predictivegrowth.service.api.DeepJavaService;
import de.tse.predictivegrowth.service.api.TrainingModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/v1/deep-java")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "DeepJava", description = "Endpoint to control deep learning models and analysis.")
public class DeepJavaController {

    private final DeepJavaService deepJavaService;

    private final TrainingModelService trainingModelService;

    @PostMapping(path = "/{stockId}")
    @ResponseStatus( code = HttpStatus.OK)
    public void trainAndSaveMlpForStockId(@PathVariable final Long stockId, @RequestParam final String instanceName) {
        this.deepJavaService.trainAndSaveMlpForStockId(instanceName, stockId);
    }

    @GetMapping(path = "/{stockId}")
    public Set<TrainingModel> findTraingingModelsForStockId(@PathVariable final Long stockId) {
        return this.trainingModelService.findTrainingModelsForStockId(stockId);
    }

    // calculate model

    //POST request new stockhistory

    //DELETE stockhistory

    // create new trainingmodel -> triggers analysis
}
