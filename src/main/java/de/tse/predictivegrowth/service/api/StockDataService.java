package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.enumeration.DataProvider;
import de.tse.predictivegrowth.model.StockHistory;
import de.tse.predictivegrowth.model.StockHistorySummary;

import java.util.List;

public interface StockDataService {

    /**
     *
     * @return
     */
    List<StockHistorySummary> findAllStockHistorySummaries();

    /**
     *
     * @param id
     * @return
     */
    StockHistory getStockHistoryById(final Long id);

    /**
     *
     * @param stockIdentifier
     * @return
     */
    StockHistorySummary requestStockHistoryFromProvider(final String stockIdentifier, final DataProvider dataProvider);

        /**
         *
         * @param stockHistory
         * @return
         */
    StockHistory saveStockHistory(final StockHistory stockHistory);

    /**
     *
     * @param id
     */
    void deleteStockHistory(final Long id);
}
