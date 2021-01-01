package de.tse.predictivegrowth.model;

import de.tse.predictivegrowth.entity.db.ModelFileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelFile {

    public ModelFile(final ModelFileEntity modelFileEntity) {
        this.id = modelFileEntity.getId();
        this.modelId = modelFileEntity.getModelId();
        this.fileEnding = modelFileEntity.getFileEnding();
        this.fileData = modelFileEntity.getFileData();
    }

    private Long id;

    private Long modelId;

    private String fileEnding;

    private byte[] fileData;
}

