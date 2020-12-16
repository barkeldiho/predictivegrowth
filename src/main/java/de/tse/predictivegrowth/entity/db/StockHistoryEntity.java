package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.model.StockHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_history")
@NoArgsConstructor
@Getter
public class StockHistoryEntity {

    public StockHistoryEntity(final StockHistory stockHistory) {
        this.stockIdentifier = stockHistory.getStockIdentifier();
        this.companyName = stockHistory.getCompanyName();

        if (stockHistory.getStockDayDataList() != null) {
            stockHistory.getStockDayDataList().forEach(stockDayData -> {
                this.addStockDataEntity(new StockDayDataEntity(stockDayData));
            });
        }

        if (stockHistory.getTrainingModelList() != null) {
            stockHistory.getTrainingModelList().forEach(trainingModel -> {
                this.addTrainingModelEntity(new TrainingModelEntity(trainingModel));
            });
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String stockIdentifier;

    private String companyName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "historyId")
    private final List<StockDayDataEntity> stockDataDayList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "historyId")
    private final List<TrainingModelEntity> trainingModelEntityList = new ArrayList<>();

    private void addStockDataEntity(final StockDayDataEntity stockDayDataEntity) {
        this.stockDataDayList.add(stockDayDataEntity);
        stockDayDataEntity.setHistoryId(this.getId());
    }

    private void addTrainingModelEntity(final TrainingModelEntity trainingModelEntity) {
        this.trainingModelEntityList.add(trainingModelEntity);
        trainingModelEntity.setHistoryId(this.getId());
    }
}
