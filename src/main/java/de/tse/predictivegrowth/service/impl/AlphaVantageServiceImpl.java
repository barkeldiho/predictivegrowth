package de.tse.predictivegrowth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tse.predictivegrowth.config.data.AlphaVantageData;
import de.tse.predictivegrowth.dao.extern.AlphaVantageDao;
import de.tse.predictivegrowth.entity.extern.AlphaVantageStockWrapper;
import de.tse.predictivegrowth.enumeration.AlphaVantageFunction;
import de.tse.predictivegrowth.enumeration.AlphaVantageOutputSize;
import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.service.api.ExtDataProviderService;
import de.tse.predictivegrowth.util.DataProcessUtil;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("alphaVantageService")
@Transactional(readOnly = true)
public class AlphaVantageServiceImpl extends AbstractRestService<AlphaVantageDao> implements ExtDataProviderService {

    private final @NonNull AlphaVantageDao alphaVantageDao;

    private final @NonNull AlphaVantageData alphaVantageData;

    @Autowired
    public AlphaVantageServiceImpl(final AlphaVantageData alphaVantageData, final ObjectMapper objectMapper) {
        super(AlphaVantageDao.class, objectMapper);
        this.alphaVantageData = alphaVantageData;

        @SuppressWarnings("StringBufferReplaceableByString")
        final String alphaVantageUrl = new StringBuilder()
                .append(alphaVantageData.getBaseUrl())
                .toString();

        this.alphaVantageDao = this.getRestInterface(alphaVantageUrl);
    }

    @Override
    public StockHistory getStockHistory(final String stockIdentifier) {

        Optional<AlphaVantageStockWrapper> alphaVantageStockWrapper;
        try {
            alphaVantageStockWrapper = Optional.ofNullable(this.alphaVantageDao.getStockHistory(
                    AlphaVantageFunction.TIME_SERIES_DAILY,
                    stockIdentifier,
                    AlphaVantageOutputSize.FULL,
                    this.alphaVantageData.getApiKey())
                    .execute()
                    .body());
        } catch (IOException e) {
            throw new RuntimeException("Problems while communicating with AlphaVantage.", e);
        }

        final List<StockDayData> stockDayDataList =
                alphaVantageStockWrapper
                        .orElseThrow(() -> new RuntimeException("Could not retrieve data from AlphaVantage for the specified stock identifier."))
                        .getTimeSeriesDaily()
                        .entrySet()
                        .stream()
                        .map((entrySet) -> {

                            final List<Double> listOfValues = new ArrayList<>();
                            listOfValues.add(entrySet.getValue().getOpen());
                            listOfValues.add(entrySet.getValue().getClose());
                            listOfValues.add(entrySet.getValue().getHigh());
                            listOfValues.add(entrySet.getValue().getLow());

                            return StockDayData.builder()
                                    .priceMean(DataProcessUtil.getMeanFromList(listOfValues))
                                    .priceVariance(DataProcessUtil.getVarianceFromList(listOfValues))
                                    .localDate(entrySet.getKey())
                                    .build();
                        })
                        .collect(Collectors.toList());

        return StockHistory.builder()
                .companyName(StringUtils.EMPTY)
                .stockDayDataList(stockDayDataList)
                .stockIdentifier(stockIdentifier)
                .build();
    }
}
