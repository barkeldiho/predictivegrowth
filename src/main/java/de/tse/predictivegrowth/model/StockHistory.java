package de.tse.predictivegrowth.model;

import lombok.Data;

import java.util.List;

@Data
public class StockHistory {

    private List<StockData> stockData;
}
