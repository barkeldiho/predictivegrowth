package de.tse.predictivegrowth.entity.extern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class AlphaVantageStockWrapper {

    @JsonProperty("Time Series (Daily)")
    public Map<LocalDate, AlphaVantageStockDayData> timeSeriesDaily = new HashMap<>();
}
