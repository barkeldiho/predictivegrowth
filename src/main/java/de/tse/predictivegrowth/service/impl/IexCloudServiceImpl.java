package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.config.data.IexCloudData;
import de.tse.predictivegrowth.dao.iex.IexCloudDao;
import de.tse.predictivegrowth.entity.iex.IexStockDayData;
import de.tse.predictivegrowth.model.StockDayData;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.service.api.IexCloudService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IexCloudServiceImpl extends AbstractRestService<IexCloudDao> implements IexCloudService {

    private final IexCloudDao iexCloudDao;

    private final IexCloudData iexCloudData;

    @Autowired
    public IexCloudServiceImpl(final IexCloudData iexCloudData) {
        super(IexCloudDao.class);
        this.iexCloudData = iexCloudData;

        @SuppressWarnings("StringBufferReplaceableByString")
        final String iexCloudUrl = new StringBuilder()
                .append(iexCloudData.getBaseUrl())
                .append(iexCloudData.getApiVersionUrl())
                .toString();

        this.iexCloudDao = this.getRestInterface(iexCloudUrl);
    }

    @Override
    public StockHistory getStockHistory(final String stockIdentifier) {

        Optional<List<IexStockDayData>> iexStockDayDataList;
        try {
            iexStockDayDataList = Optional.ofNullable(this.iexCloudDao.getStockHistoryForTimespan(stockIdentifier,
                    this.iexCloudData.getMaxTimespan(),
                    this.iexCloudData.getPublishableToken())
                    .execute()
                    .body());
        } catch (IOException e) {
            throw new RuntimeException("Problems while communicating with IEXCloud.");
        }

        final List<StockDayData> stockDayDataList =
                iexStockDayDataList
                .orElseThrow(() -> new RuntimeException("Could not retrieve data from IEXCloud for the specified stock identifier."))
                .stream()
                .map(data -> StockDayData.builder()
                        .priceMean(this.getMeanFromIexData(data))
                        .priceVariance(this.getVarianceFromIexData(data))
                        .localDate(data.getDate())
                        .build())
                .collect(Collectors.toList());

        return StockHistory.builder()
                .companyName(StringUtils.EMPTY)
                .stockDayDataList(stockDayDataList)
                .stockIdentifier(stockIdentifier)
                .build();
    }

    private double getVarianceFromIexData(final IexStockDayData data) {
        return new Variance().evaluate(
                new double[]{data.getClose(),
                        data.getOpen(),
                        data.getHigh(),
                        data.getLow()}
        );
    }

    private double getMeanFromIexData(final IexStockDayData data) {
        return new Mean().evaluate(
                new double[]{data.getClose(),
                        data.getOpen(),
                        data.getHigh(),
                        data.getLow()}
        );
    }
}
