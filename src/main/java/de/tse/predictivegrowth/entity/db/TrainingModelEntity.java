package de.tse.predictivegrowth.entity.db;

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
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ElementCollection
    private List<Integer> layerUnits = new ArrayList<>();

    private Integer inputLayer;

    private String instanceName;

    private Long historyId;

    private Integer status;

    private byte[] modelFile;
}
