package de.tse.predictivegrowth.controller;

import de.tse.predictivegrowth.service.api.DeepJavaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(DeepJavaController.DL_PATH)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "DeepJava", description = "Endpoint to control deep learning models and analysis.")
public class DeepJavaController {

    public static final String DL_PATH = "/v1/deep-learning";

    private final DeepJavaService deepJavaService;

    @Operation(summary = "Method uses a training model to create an anctual DJL model.",
            description = "Method uses a training model and its properties to train a DJL model which is then saved as model file for the training model.")
    @PostMapping(path = "/{modelId}/train")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void trainAndSaveMlpForModel(@PathVariable final Long modelId) {
        this.deepJavaService.trainAndSaveMlpForModel(modelId);
    }

    @Operation(summary = "Method uses a training model to create an anctual DJL model.",
            description = "Method uses a training model and its properties to train a DJL model which is then saved as model file for the training model.")
    @GetMapping(path = "/{modelId}/predict/{outputCount}")
    public List<Double> getRollingPredictionForModel(@PathVariable final Long modelId, @PathVariable final Integer outputCount, @RequestParam(required = false) Integer start) {
        start = (start == null) ? 0 : start;
        return this.deepJavaService.getRollingPredictionForModel(modelId, outputCount, start);
    }
}
