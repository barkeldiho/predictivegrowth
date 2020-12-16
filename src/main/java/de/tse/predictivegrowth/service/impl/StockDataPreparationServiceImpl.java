package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class StockDataPreparationServiceImpl implements StockDataPreparationService {

    @Override
    public List<StockDayData> fullyPrepare(final List<StockDayData> stockDayDataList) {
        final List<StockDayData> locfList = this.locf(stockDayDataList);
        return this.normalize(locfList);

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

    private Double getNormalizedValueForMinMax(final Double value, final Double max, final Double min) {
        return (value - min) / (max - min);
        // denormalized_d = normalized_d * (max_d - min_d) + min_d
    }
}
