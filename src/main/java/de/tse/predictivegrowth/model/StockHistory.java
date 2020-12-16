package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.StockHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistory {

    public StockHistory(final StockHistoryEntity entity) {
        this.stockIdentifier = entity.getStockIdentifier();
        this.companyName = entity.getCompanyName();
        this.stockDayDataList = entity.getStockDataDayList().stream()
                .map(StockDayData::new).collect(Collectors.toList());
        this.trainingModelList = entity.getTrainingModelEntityList().stream()
                .map(TrainingModel::new).collect(Collectors.toList());
        this.id = entity.getId();
    }

    private Long id;

    private String stockIdentifier;

    private String companyName;

    private List<StockDayData> stockDayDataList = new ArrayList<>();

    private List<TrainingModel> trainingModelList = new ArrayList<>();
}
