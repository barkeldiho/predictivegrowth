package de.tse.predictivegrowth.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StockDataDay {

    private LocalDate localDate;

    private Long open;

    private Long close;

    private Long high;

    private Long low;
}
