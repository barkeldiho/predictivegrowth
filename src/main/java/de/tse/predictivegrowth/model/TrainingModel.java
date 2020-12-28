package de.tse.predictivegrowth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.tse.predictivegrowth.entity.db.TrainingModelEntity;
import de.tse.predictivegrowth.enumeration.TrainingStatus;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModel {

    public TrainingModel(final TrainingModelEntity trainingModelEntity) {
        this.inputLayer = trainingModelEntity.getInputLayer();
        this.instanceName = trainingModelEntity.getInstanceName();
        this.layerUnits = trainingModelEntity.getLayerUnits();
        this.status = trainingModelEntity.getStatus();
        this.modelFile = trainingModelEntity.getModelFile();
        this.historyId = trainingModelEntity.getHistoryId();
        this.trainingIntStart = trainingModelEntity.getTrainingIntStart();
        this.trainingIntEnd = trainingModelEntity.getTrainingIntEnd();
        this.trainingIntMax = trainingModelEntity.getTrainingIntMax();
        this.trainingIntMin = trainingModelEntity.getTrainingIntMin();
        this.outputLayer = trainingModelEntity.getOutputLayer();
    }

    @NotEmpty
    private List<@Min(1) Integer> layerUnits = new ArrayList<>();

    @Min(1)
    private Integer inputLayer;

    @Min(1)
    private Integer outputLayer;

    @NotBlank
    private String instanceName;

    @NotNull
    private Long trainingIntStart;

    @NotNull
    private Long trainingIntEnd;

    @JsonIgnore
    private Double trainingIntMax;

    @JsonIgnore
    private Double trainingIntMin;

    private TrainingStatus status = TrainingStatus.NONE;

    @JsonIgnore
    private byte[] modelFile;

    @NotNull
    private Long historyId;
}
