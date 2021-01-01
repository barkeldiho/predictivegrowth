package de.tse.predictivegrowth.entity.db;

import de.tse.predictivegrowth.model.ModelFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "model_file")
@NoArgsConstructor
@Setter
@Getter
public class ModelFileEntity {

    public ModelFileEntity(final ModelFile modelFile) {
        this.id = modelFile.getId();
        this.modelId = modelFile.getModelId();
        this.fileEnding = modelFile.getFileEnding();
        this.fileData = modelFile.getFileData();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long modelId;

    private String fileEnding;

    private byte[] fileData;
}
