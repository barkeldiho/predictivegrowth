package de.tse.predictivegrowth.service.api;

import de.tse.predictivegrowth.model.InOutData;
import de.tse.predictivegrowth.model.NormalizationData;
import de.tse.predictivegrowth.model.StockDayData;
import org.javatuples.Pair;

import java.util.List;

public interface StockDataPreparationService {

    Pair<InOutData, NormalizationData> fullyPrepare(final List<StockDayData> stockDayDataList, final Integer seriesSize, final Long trainingIntStart, final Long trainingIntEnd);
}
