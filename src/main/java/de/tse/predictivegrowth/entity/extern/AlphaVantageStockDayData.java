package de.tse.predictivegrowth.entity.extern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class AlphaVantageStockDayData {

    @JsonProperty("1. open")
    private final Double open;

    @JsonProperty("4. close")
    private final Double close;

    @JsonProperty("2. high")
    private final Double high;

    @JsonProperty("3. low")
    private final Double low;
}
