package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.StockHistory;

public interface IexCloudService {

    /**
     *
     * @param stockIdentifier
     * @return
     */
    StockHistory getStockHistory(final String stockIdentifier);
}
