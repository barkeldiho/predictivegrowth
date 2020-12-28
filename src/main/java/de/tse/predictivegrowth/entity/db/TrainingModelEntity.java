package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.enumeration.TrainingStatus;
import de.tse.predictivegrowth.model.TrainingModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_model")
@NoArgsConstructor
@Setter
@Getter
public class TrainingModelEntity {

    public TrainingModelEntity(final TrainingModel trainingModel) {
        this.layerUnits = trainingModel.getLayerUnits();
        this.inputLayer = trainingModel.getInputLayer();
        this.instanceName = trainingModel.getInstanceName();
        this.status = trainingModel.getStatus();
        this.modelFile = trainingModel.getModelFile();
        this.historyId = trainingModel.getHistoryId();
        this.trainingIntStart = trainingModel.getTrainingIntStart();
        this.trainingIntEnd = trainingModel.getTrainingIntEnd();
        this.trainingIntMax = trainingModel.getTrainingIntMax();
        this.trainingIntMin = trainingModel.getTrainingIntMin();
        this.outputLayer = trainingModel.getOutputLayer();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ElementCollection
    private List<Integer> layerUnits = new ArrayList<>();

    private Integer inputLayer;

    private Integer outputLayer;

    private String instanceName;

    private Long trainingIntStart;

    private Long trainingIntEnd;

    private Double trainingIntMax;

    private Double trainingIntMin;

    private Long historyId;

    @Enumerated(EnumType.STRING)
    private TrainingStatus status = TrainingStatus.NONE;

    private byte[] modelFile;
}
