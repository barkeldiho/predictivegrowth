package de.tse.predictivegrowth.service.api;

import java.io.IOException;
import java.util.List;

public interface DeepJavaService {

    void trainAndSaveMlpForModel(final Long modelId);

    List<Double> getRollingPredictionForModel(final Long modelId, final Integer outputCount);
}
