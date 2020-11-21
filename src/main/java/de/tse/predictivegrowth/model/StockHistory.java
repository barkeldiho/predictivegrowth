package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class StockHistory {

    public StockHistory(final StockHistoryEntity entity) {
        this.stockIdentifier = entity.getStockIdentifier();
        this.companyName = entity.getCompanyName();
        this.stockDayDataList = entity.getStockDataDayList().stream().map(StockDayData::new).collect(Collectors.toList());
    }

    private final String stockIdentifier;

    private final String companyName;

    private final List<StockDayData> stockDayDataList;
}
