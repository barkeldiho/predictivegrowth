package de.tse.predictivegrowth.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlphaVantageOutputSize {

    COMPACT("compact"),
    FULL("full");

    private final String outputSize;

    public String getValue() {
        return this.outputSize;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
