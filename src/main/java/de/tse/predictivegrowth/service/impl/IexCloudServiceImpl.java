package de.tse.predictivegrowth.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tse.predictivegrowth.config.data.IexCloudData;
import de.tse.predictivegrowth.dao.extern.IexCloudDao;
import de.tse.predictivegrowth.entity.extern.IexStockDayData;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("iexCloudService")
@Transactional(readOnly = true)
public class IexCloudServiceImpl extends AbstractRestService<IexCloudDao> implements ExtDataProviderService {

    private final @NonNull IexCloudDao iexCloudDao;

    private final @NonNull IexCloudData iexCloudData;

    @Autowired
    public IexCloudServiceImpl(final IexCloudData iexCloudData, final ObjectMapper objectMapper) {
        super(IexCloudDao.class, objectMapper);
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
            iexStockDayDataList = Optional.ofNullable(this.iexCloudDao.getListOfIexStockDayData(stockIdentifier,
                    this.iexCloudData.getMaxTimespan(),
                    this.iexCloudData.getPublishableToken())
                    .execute()
                    .body());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problems while communicating with IEXCloud.");
        }

        final List<StockDayData> stockDayDataList =
                iexStockDayDataList
                .orElseThrow(() -> new RuntimeException("Could not retrieve data from IEXCloud for the specified stock identifier."))
                .stream()
                .map(data -> {
                    final List<Double> listOfValues = new ArrayList<>();
                    listOfValues.add(data.getOpen());
                    listOfValues.add(data.getClose());
                    listOfValues.add(data.getHigh());
                    listOfValues.add(data.getLow());

                    final LocalDate localDate = LocalDate.parse(data.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);

                    return StockDayData.builder()
                            .priceMean(DataProcessUtil.getMeanFromList(listOfValues))
                            .priceVariance(DataProcessUtil.getVarianceFromList(listOfValues))
                            .localDate(localDate)
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
