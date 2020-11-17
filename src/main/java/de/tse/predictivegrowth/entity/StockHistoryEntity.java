package de.tse.predictivegrowth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String stockIdentifier;

    private String companyName;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StockDataEntity> stockDataDayList;

    public void addStockDataEntity(final StockDataEntity stockDataEntity) {
        this.stockDataDayList.add(stockDataEntity);
        stockDataEntity.setHistory(this);
    }

    public void removeStockDataEntity(final StockDataEntity stockDataEntity) {
        this.stockDataDayList.remove(stockDataEntity);
        stockDataEntity.setHistory(null);
    }
}
