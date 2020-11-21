package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.StockDayData;

import java.util.List;

public interface StockDataPreparationService {

    List<StockDayData> prepare(final List<StockDayData> stockDayDataList);
}
