package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.dao.db.StockHistoryDao;
import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import de.tse.predictivegrowth.enumeration.DataProvider;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.model.StockHistorySummary;
import de.tse.predictivegrowth.service.api.ExtDataProviderService;
import de.tse.predictivegrowth.service.api.StockDataService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional(readOnly = true)
public class StockDataServiceImpl implements StockDataService {

    @Qualifier("iexCloudService")
    private final @NonNull ExtDataProviderService iexCloudService;

    @Qualifier("alphaVantageService")
    private final @NonNull ExtDataProviderService alphaVantageService;

    private final @NonNull StockHistoryDao stockHistoryDao;

    @Override
    public List<StockHistorySummary> findAllStockHistorySummaries() {
        return this.stockHistoryDao.findAll().stream()
            .map(StockHistorySummary::new)
            .collect(Collectors.toList());
    }

    public StockHistory getStockHistory(final Long id) {
        return this.stockHistoryDao.findById(id)
                .map(StockHistory::new)
                .orElseThrow(() -> new RuntimeException(String.format("Could not find StockHistory for id = %d.", id)));
    }

    @Override
    @Transactional
    public StockHistorySummary requestStockHistoryFromProvider(final String stockIdentifier, final DataProvider dataProvider) {
        StockHistory stockHistory;
        switch (dataProvider) {
            case IEXCLOUD:
                stockHistory = this.iexCloudService.getStockHistory(stockIdentifier);
                break;
            case ALPHAVANTAGE:
                stockHistory = this.alphaVantageService.getStockHistory(stockIdentifier);
                break;
            default:
                throw new RuntimeException("Could not find data provider.");
        }
        final StockHistory savedStockHistory = this.saveStockHistory(stockHistory);
        return new StockHistorySummary(savedStockHistory);
    }

    @Override
    @Transactional
    public StockHistory saveStockHistory(final StockHistory stockHistory) {
         final StockHistoryEntity entity = this.stockHistoryDao.save(new StockHistoryEntity(stockHistory));
         return new StockHistory(entity);
    }
}
