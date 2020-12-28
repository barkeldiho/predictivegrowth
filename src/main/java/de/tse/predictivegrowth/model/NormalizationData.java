package de.tse.predictivegrowth.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NormalizationData {

    private final Double trainingIntMin;

    private final Double trainingIntMax;
}
