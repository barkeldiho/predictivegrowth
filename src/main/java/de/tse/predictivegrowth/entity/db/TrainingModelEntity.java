package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.enumeration.TrainingStatus;
import de.tse.predictivegrowth.model.TrainingModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "training_model")
@NoArgsConstructor
@Setter
@Getter
public class TrainingModelEntity {

    public TrainingModelEntity(final TrainingModel trainingModel) {
        if (trainingModel.getLayerUnits() != null) {
            this.layerUnits = trainingModel.getLayerUnits();
        }
        this.inputLayer = trainingModel.getInputLayer();
        this.instanceName = trainingModel.getInstanceName();
        this.status = trainingModel.getStatus();
        if (trainingModel.getModelFiles() != null) {
            this.modelFiles = trainingModel.getModelFiles().stream()
                    .map(ModelFileEntity::new)
                    .collect(Collectors.toList());
        }
        this.historyId = trainingModel.getHistoryId();
        this.trainingIntStart = trainingModel.getTrainingIntStart();
        this.trainingIntEnd = trainingModel.getTrainingIntEnd();
        this.trainingIntMax = trainingModel.getTrainingIntMax();
        this.trainingIntMin = trainingModel.getTrainingIntMin();
        if (trainingModel.getR2() != null) {
            this.r2 = trainingModel.getR2();
        }
        if (trainingModel.getMae() != null) {
            this.mae = trainingModel.getMae();
        }
        if (trainingModel.getRmse() != null) {
            this.rmse = trainingModel.getRmse();
        }
        if (trainingModel.getTlccMaxLag() != null) {
            this.tlccMaxLag = trainingModel.getTlccMaxLag();
        }
        this.outputLayer = trainingModel.getOutputLayer();
        this.id = trainingModel.getId();
        this.epochs = trainingModel.getEpochs();
        this.verificationIntStart = trainingModel.getVerificationIntStart();
        this.verificationIntEnd = trainingModel.getVerificationIntEnd();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Integer> layerUnits = new ArrayList<>();

    private Integer inputLayer;

    private Integer outputLayer;

    private String instanceName;

    private Long trainingIntStart;

    private Long trainingIntEnd;

    private Long verificationIntStart;

    private Long verificationIntEnd;

    private Integer epochs;

    private Double trainingIntMax;

    private Double trainingIntMin;

    private Double r2;

    private Double mae;

    private Double rmse;

    private Integer tlccMaxLag;

    private Long historyId;

    @Enumerated(EnumType.STRING)
    private TrainingStatus status = TrainingStatus.NONE;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "modelId")
    private List<ModelFileEntity> modelFiles = new ArrayList<>();
}
