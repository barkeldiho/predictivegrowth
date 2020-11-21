package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.StockDayDataEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class StockDayData {

    public StockDayData(final StockDayDataEntity entity) {
        this.localDate = entity.getLocalDate();
        this.priceMean = entity.getPriceMean();
        this.priceVariance = entity.getPriceVariance();
    }

    private LocalDate localDate;

    private Double priceMean;

    private Double priceVariance;
}
