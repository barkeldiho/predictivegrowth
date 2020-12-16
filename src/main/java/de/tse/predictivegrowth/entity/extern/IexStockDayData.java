package de.tse.predictivegrowth.entity.extern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IexStockDayData {

    private String date;

    private Double open;

    private Double close;

    private Double high;

    private Double low;
}
