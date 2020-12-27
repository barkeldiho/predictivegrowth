package de.tse.predictivegrowth.service.api;

public interface DeepJavaService {

    void trainAndSaveMlpForStockId(final String instanceName, final Long stockId, final Double trainingSetSize);
}
