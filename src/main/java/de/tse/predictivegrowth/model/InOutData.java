package de.tse.predictivegrowth.model;

import ai.djl.ndarray.NDArray;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class InOutData {

    private final NDArray inputs;

    private final NDArray labels;
}
