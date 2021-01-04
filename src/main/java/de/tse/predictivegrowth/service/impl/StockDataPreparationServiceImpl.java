package de.tse.predictivegrowth.service.impl;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import de.tse.predictivegrowth.model.InOutData;
import de.tse.predictivegrowth.model.NormalizationData;
import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import de.tse.predictivegrowth.util.DataProcessUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class StockDataPreparationServiceImpl implements StockDataPreparationService {

    private final NDManager ndManager = NDManager.newBaseManager();

    @Override
    public Pair<InOutData, NormalizationData> fullyPrepare(final List<StockDayData> stockDayDataList, final Integer seriesSize,
                                                           final Long trainingIntStart, final Long trainingIntEnd) {
        final List<StockDayData> training = this.cutToTrainingSet(stockDayDataList, trainingIntStart, trainingIntEnd);
        final List<StockDayData> locfList = this.locf(training);
        final List<StockDayData> normalized = this.normalize(locfList);

        final Double trainingIntMin = Collections.min(training.stream().map(StockDayData::getPriceMean).collect(Collectors.toSet()));
        final Double trainingIntMax = Collections.max(training.stream().map(StockDayData::getPriceMean).collect(Collectors.toSet()));
        final NormalizationData normalizationData = new NormalizationData(trainingIntMin, trainingIntMax);
        return new Pair<>(this.getInOutData(normalized, seriesSize), normalizationData);
    }

    private List<StockDayData> cutToTrainingSet(final List<StockDayData> stockDayDataList, final Long trainingIntStart, final Long trainingIntEnd) {
        if (stockDayDataList.size() < (trainingIntEnd | trainingIntStart)
                || (trainingIntEnd | trainingIntStart) < 0
                || trainingIntEnd < trainingIntStart) {
            throw new RuntimeException("Training interval incorrect.");
        }
        final List<StockDayData> trainingSet = new ArrayList<>();
        for (int i = trainingIntStart.intValue(); i < trainingIntEnd.intValue(); i++) {
            trainingSet.add(stockDayDataList.get(i));
        }

        return trainingSet;
    }

    // Last Observation Carried Forward (LOCF) For StockDayData
    private List<StockDayData> locf(final List<StockDayData> stockDayDataList) {
        final List<StockDayData> result = new ArrayList<>();

        StockDayData lastStockDayData = null;
        for (final StockDayData currentStockDayData : stockDayDataList) {
            if (lastStockDayData != null) {
                if (currentStockDayData.getLocalDate() == null) {
                    log.debug("Removed StockDayData item due to null date.");
                    continue;
                }

                if (currentStockDayData.getPriceMean() == null) {
                    currentStockDayData.setPriceMean(lastStockDayData.getPriceMean());
                }

                if (currentStockDayData.getPriceVariance() == null) {
                    currentStockDayData.setPriceVariance(lastStockDayData.getPriceVariance());
                }
            }
            result.add(currentStockDayData);
            lastStockDayData = currentStockDayData;
        }
        return result;
    }

    private List<StockDayData> normalize(final List<StockDayData> stockDayDataList) {

        final List<Double> priceMeans = stockDayDataList.stream()
                .map(StockDayData::getPriceMean)
                .collect(Collectors.toList());

        final Double mean_max = Collections.max(priceMeans);
        final Double mean_min = Collections.min(priceMeans);

        final List<Double> normalizedPriceMeans = priceMeans.stream()
                .map(val -> DataProcessUtil.getNormalizedValueForMinMax(val, mean_max, mean_min))
                .collect(Collectors.toList());

        if (stockDayDataList.size() != priceMeans.size()) {
            throw new RuntimeException("List sizes do not match during normalization.");
        }

        final List<StockDayData> result = new ArrayList<>();
        for (int i=0; i < stockDayDataList.size(); i++) {
            final StockDayData resVal = StockDayData.builder()
                    .localDate(stockDayDataList.get(i).getLocalDate())
                    .priceVariance(stockDayDataList.get(i).getPriceVariance())
                    .priceMean(normalizedPriceMeans.get(i))
                    .build();
            result.add(resVal);
        }

        return result;
    }

    private InOutData getInOutData(final List<StockDayData> stockDayDataList, final Integer seriesSize) {
        final List<Double> meanData = stockDayDataList.stream()
                .map(StockDayData::getPriceMean)
                .collect(Collectors.toList());

        final int setSize = meanData.size()-seriesSize+1;

        final float[] meanDataArrayInputs = new float[setSize*seriesSize];
        for (int i = 0 ; i < setSize; i++) {
            int arrayIndex = i * seriesSize;
            for (int j = i; j < i + seriesSize; j++) {
                meanDataArrayInputs[arrayIndex] = meanData.get(j).floatValue();
                arrayIndex++;
            }
        }

        final List<Double> outputVals = new ArrayList<>();
        for (int s = seriesSize; s < meanData.size(); s++) {
            outputVals.add(meanData.get(s));
        }
        outputVals.add(meanData.get(meanData.size()-1)); // last value carry forward for last set of data points

        final float[] meanDataArrayOutputs = new float[outputVals.size()];
        for (int i = 0 ; i < outputVals.size(); i++) {
            meanDataArrayOutputs[i] = outputVals.get(i).floatValue();
        }

        final NDArray inputs = this.ndManager.create(meanDataArrayInputs).reshape(new Shape(setSize, seriesSize));
        final NDArray outputs = this.ndManager.create(meanDataArrayOutputs).reshape(new Shape(setSize, 1));

        return new InOutData(inputs, outputs);
    }
}
