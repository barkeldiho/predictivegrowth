package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.service.api.StockDataPreparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class StockDataPreparationServiceImpl implements StockDataPreparationService {

    @Override
    public List<StockDayData> prepare(final List<StockDayData> stockDayDataList) {
        return this.normalize(this.locf(stockDayDataList));
    }

    // Lats Observation Carried Forward (LOCF) For StockDayData
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
        final List<Double> priceMeans = new ArrayList<>();
        final List<Double> priceVariances = new ArrayList<>();

        for (StockDayData stockDayData : stockDayDataList) {
            priceMeans.add(stockDayData.getPriceMean());
            priceVariances.add(stockDayData.getPriceVariance());
        }

        final List<Double> normalizedPriceMeans = this.normalizeListViaStatUtils(priceMeans);
        final List<Double> normalizedPriceVariances = this.normalizeListViaStatUtils(priceVariances);

        final Iterator<StockDayData> itData = stockDayDataList.iterator();
        final Iterator<Double> itMeans = priceMeans.iterator();
        final Iterator<Double> itVariances = priceVariances.iterator();

        if (stockDayDataList.size() != (priceMeans.size() | priceVariances.size())) {
            throw new RuntimeException("List sizes do not match during normalization.");
        }

        while(itData.hasNext()) {
            final StockDayData stockDayData = itData.next();
            final Double priceMean = itMeans.next();
            final Double priceVariance = itVariances.next();
            stockDayData.setPriceMean(priceMean);
            stockDayData.setPriceVariance(priceVariance);
        }

        return stockDayDataList;
    }

    private List<Double> normalizeListViaStatUtils(final List<Double> valueList) {
        return Arrays.asList(
                ArrayUtils.toObject(
                        StatUtils.normalize(ArrayUtils.toPrimitive(valueList.toArray(Double[]::new)))
                )
        );
    }
}
