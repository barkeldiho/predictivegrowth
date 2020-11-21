package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.StockHistory;

public interface StockDataService {

    /**
     * Method returns historic stock data for the stock identified by the supplied stock identifier.
     *
     * @param stockIdentifier identifies the stock.
     * @return the complete {@link de.tse.predictivegrowth.model.StockHistory} of a stock
     */
    StockHistory getStockHistory(final String stockIdentifier);

    /**
     *
     * @param stockHistory
     * @return
     */
    StockHistory saveStockHistory(final StockHistory stockHistory);
}
