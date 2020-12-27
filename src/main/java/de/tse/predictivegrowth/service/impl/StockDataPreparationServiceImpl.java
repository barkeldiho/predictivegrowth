package de.tse.predictivegrowth.service.impl;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import de.tse.predictivegrowth.model.InOutData;
import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public InOutData fullyPrepare(final List<StockDayData> stockDayDataList, final Double trainingSetSize) {
        final List<StockDayData> training = this.cutToTrainingSet(stockDayDataList, trainingSetSize);
        final List<StockDayData> locfList = this.locf(training);
        final List<StockDayData> normalized = this.normalize(locfList);
        return this.getInOutData(normalized, 35);
    }

    private List<StockDayData> cutToTrainingSet(final List<StockDayData> stockDayDataList, final Double trainingSetSize) {
        final long trainingIndex = Math.round(stockDayDataList.size()*trainingSetSize);
        final List<StockDayData> trainingSet = new ArrayList<>();
        for (int i = 0; i<trainingIndex; i++) {
            trainingSet.add(stockDayDataList.get(i));
        }

        return trainingSet;
    }

    // Last Observation Carried Forward (LOCF) For StockDayData
    private List<StockDayData> locf(final List<StockDayData> stockDayDataList) {
        final Iterator<StockDayData> iterator = stockDayDataList.iterator();
        StockDayData lastStockDayData = null;
        while(iterator.hasNext()) {

            final StockDayData currentStockDayData = iterator.next();

            if (lastStockDayData != null) {
                if (currentStockDayData.getLocalDate() == null) {
                    log.debug("Removed StockDayData item due to null date.");
                    iterator.remove();
                    continue;
                }

                if (currentStockDayData.getPriceMean() == null) {
                    currentStockDayData.setPriceMean(lastStockDayData.getPriceMean());
                }

                if (currentStockDayData.getPriceVariance() == null) {
                    currentStockDayData.setPriceVariance(lastStockDayData.getPriceVariance());
                }
            }

            lastStockDayData = currentStockDayData;
        }

        return stockDayDataList;
    }

    private List<StockDayData> normalize(final List<StockDayData> stockDayDataList) {
        final List<Double> priceMeans = stockDayDataList.stream()
                .map(StockDayData::getPriceMean)
                .collect(Collectors.toList());

        final Double mean_max = Collections.max(priceMeans);
        final Double mean_min = Collections.min(priceMeans);

        final List<Double> normalizedPriceMeans = priceMeans.stream()
                .map(val -> this.getNormalizedValueForMinMax(val, mean_max, mean_min))
                .collect(Collectors.toList());


        final Iterator<StockDayData> itData = stockDayDataList.iterator();
        final Iterator<Double> itMeans = normalizedPriceMeans.iterator();

        if (stockDayDataList.size() != priceMeans.size()) {
            throw new RuntimeException("List sizes do not match during normalization.");
        }

        while(itData.hasNext()) {
            final StockDayData stockDayData = itData.next();
            final Double priceMean = itMeans.next();
            stockDayData.setPriceMean(priceMean);
        }

        return stockDayDataList;
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

    private Double getNormalizedValueForMinMax(final Double value, final Double max, final Double min) {
        return (value - min) / (max - min);
        // denormalized_d = normalized_d * (max_d - min_d) + min_d
    }
}
