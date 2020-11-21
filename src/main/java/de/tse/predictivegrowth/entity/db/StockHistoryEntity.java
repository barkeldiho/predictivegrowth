package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.model.StockHistory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "stock_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockHistoryEntity {

    public StockHistoryEntity(final StockHistory stockHistory) {
        this.stockIdentifier = stockHistory.getStockIdentifier();
        this.companyName = stockHistory.getCompanyName();
        stockHistory.getStockDayDataList().forEach(item -> {
            final StockDayDataEntity stockDayDataEntity = new StockDayDataEntity(item);
            this.addStockDataEntity(stockDayDataEntity);
        });
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String stockIdentifier;

    private String companyName;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockDayDataEntity> stockDataDayList;

    public void addStockDataEntity(final StockDayDataEntity stockDayDataEntity) {
        this.stockDataDayList.add(stockDayDataEntity);
        stockDayDataEntity.setHistory(this);
    }

    public void removeStockDataEntity(final StockDayDataEntity stockDayDataEntity) {
        this.stockDataDayList.remove(stockDayDataEntity);
        stockDayDataEntity.setHistory(null);
    }
}
