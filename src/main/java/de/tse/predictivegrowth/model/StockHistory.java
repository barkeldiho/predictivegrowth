package de.tse.predictivegrowth.model;

import lombok.Data;

import java.util.List;

@Data
public class StockHistory {

    private final String stockIdentifier;

    private final String companyName;

    private final List<StockDataDay> stockDataDayList;
}
