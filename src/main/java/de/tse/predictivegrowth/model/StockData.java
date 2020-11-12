package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.service.impl.AbstractRestService;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StockData extends AbstractRestService<StockData> {

    private LocalDate localDate;

    private Long open;

    private Long close;

    private Long high;

    private Long low;
}
