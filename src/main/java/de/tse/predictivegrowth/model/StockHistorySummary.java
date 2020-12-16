package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StockHistorySummary {

    public StockHistorySummary(final StockHistoryEntity entity) {
        this.stockIdentifier = entity.getStockIdentifier();
        this.companyName = entity.getCompanyName();
        this.id = entity.getId();
    }

    public StockHistorySummary(final StockHistory stockHistory) {
        this.stockIdentifier = stockHistory.getStockIdentifier();
        this.companyName = stockHistory.getCompanyName();
        this.id = stockHistory.getId();
    }

    private Long id;

    private String stockIdentifier;

    private String companyName;
}
