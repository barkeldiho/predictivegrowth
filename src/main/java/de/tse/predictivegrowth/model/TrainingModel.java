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
import java.util.stream.Collectors;

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
        this.modelFiles = trainingModelEntity.getModelFiles().stream().map(ModelFile::new).collect(Collectors.toList());
        this.historyId = trainingModelEntity.getHistoryId();
        this.trainingIntStart = trainingModelEntity.getTrainingIntStart();
        this.trainingIntEnd = trainingModelEntity.getTrainingIntEnd();
        this.trainingIntMax = trainingModelEntity.getTrainingIntMax();
        this.trainingIntMin = trainingModelEntity.getTrainingIntMin();
        if (trainingModelEntity.getR2() != null) {
            this.r2 = trainingModelEntity.getR2();
        }
        if (trainingModelEntity.getMae() != null) {
            this.mae = trainingModelEntity.getMae();
        }
        if (trainingModelEntity.getRmse() != null) {
            this.rmse = trainingModelEntity.getRmse();
        }
        if (trainingModelEntity.getTlccMaxLag() != null) {
            this.tlccMaxLag = trainingModelEntity.getTlccMaxLag();
        }
        this.outputLayer = trainingModelEntity.getOutputLayer();
        this.id = trainingModelEntity.getId();
        this.epochs = trainingModelEntity.getEpochs();
        this.verificationIntStart = trainingModelEntity.getVerificationIntStart();
        this.verificationIntEnd = trainingModelEntity.getVerificationIntEnd();
    }

    private Long id;

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

    @NotNull
    private Long verificationIntStart;

    @NotNull
    private Long verificationIntEnd;

    @NotNull
    private Integer epochs;

    @JsonIgnore
    private Double trainingIntMax;

    @JsonIgnore
    private Double trainingIntMin;

    private Double r2;

    private Double mae;

    private Double rmse;

    private Integer tlccMaxLag;

    private TrainingStatus status = TrainingStatus.NONE;

    @JsonIgnore
    private List<ModelFile> modelFiles = new ArrayList<>();

    @NotNull
    private Long historyId;
}
