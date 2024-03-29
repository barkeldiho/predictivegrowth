package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.model.StockDayData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock_data")
@NoArgsConstructor
@Setter
@Getter
public class StockDayDataEntity {

    public StockDayDataEntity(final StockDayData stockDayData) {
        this.localDate = stockDayData.getLocalDate();
        this.priceMean = stockDayData.getPriceMean();
        this.priceVariance = stockDayData.getPriceVariance();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long historyId;

    private LocalDate localDate;

    private Double priceMean;

    private Double priceVariance;
}
