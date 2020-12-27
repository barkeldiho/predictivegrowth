package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.InOutData;
import de.tse.predictivegrowth.model.StockDayData;

import java.util.List;

public interface StockDataPreparationService {

    InOutData fullyPrepare(final List<StockDayData> stockDayDataList, final Double trainingSetSize);
}
