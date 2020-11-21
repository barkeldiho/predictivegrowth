package de.tse.predictivegrowth.service.impl;

import de.tse.predictivegrowth.dao.db.StockHistoryDao;
import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.service.api.IexCloudService;
import de.tse.predictivegrowth.service.api.StockDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StockDataServiceImpl implements StockDataService {

    private final IexCloudService iexCloudService;

    private final StockHistoryDao stockHistoryDao;

    @Override
    public StockHistory getStockHistory(final String stockIdentifier) {
        return this.stockHistoryDao.findByStockIdentifier(stockIdentifier)
                .map(StockHistory::new)
                .orElseGet(() -> {
                    final StockHistory stockHistory = this.iexCloudService.getStockHistory(stockIdentifier);
                    return this.saveStockHistory(stockHistory);
                });
    }

    @Override
    public StockHistory saveStockHistory(final StockHistory stockHistory) {
         final StockHistoryEntity entity = this.stockHistoryDao.save(new StockHistoryEntity(stockHistory));
         return new StockHistory(entity);
    }
}
