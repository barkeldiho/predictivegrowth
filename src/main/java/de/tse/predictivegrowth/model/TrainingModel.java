package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.TrainingModelEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
        this.modelFile = trainingModelEntity.getModelFile();
        this.historyId = trainingModelEntity.getHistoryId();
    }

    private List<Integer> layerUnits = new ArrayList<>();

    private Integer inputLayer;

    private String instanceName;

    private Integer status;

    private byte[] modelFile;

    private Long historyId;
}
