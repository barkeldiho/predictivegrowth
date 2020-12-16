package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.TrainingModelEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModel {

    public TrainingModel(final TrainingModelEntity trainingModelEntity) {
        this.inputLayer = trainingModelEntity.getInputLayer();
        this.instanceName = trainingModelEntity.getInstanceName();
        this.layerUnits = trainingModelEntity.getLayerUnits();
        this.status = trainingModelEntity.getStatus();
        this.historyId = trainingModelEntity.getHistoryId();
        this.modelFile = trainingModelEntity.getModelFile();
    }

    private List<Integer> layerUnits;

    private Integer inputLayer;

    private String instanceName;

    private Integer status;

    private Long historyId;

    private byte[] modelFile;
}
