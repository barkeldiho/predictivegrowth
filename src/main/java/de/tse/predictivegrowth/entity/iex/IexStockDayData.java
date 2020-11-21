package de.tse.predictivegrowth.entity.iex;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class IexStockDayData {

    private LocalDate date;

    private Long open;

    private Long close;

    private Long high;

    private Long low;
}
