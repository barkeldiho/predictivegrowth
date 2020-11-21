package de.tse.predictivegrowth.dataset;

import ai.djl.training.dataset.RandomAccessDataset;
import de.tse.predictivegrowth.model.StockDayData;
import liquibase.util.csv.opencsv.CSVParser;
import org.tensorflow.op.core.CSVDataset;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class StockDataset extends RandomAccessDataset {

    private final List<StockDayData> stockDayDataList;

    private StockDataset(Builder builder) {
        super(builder);
        stockDayDataList = builder.stockDayDataList;
    }

    public static final class Builder extends BaseBuilder<Builder> {
        List<StockDayData> stockDayDataList;

        @Override
        protected Builder self() {
            return this;
        }

        List<StockDayData> build() throws IOException {
            String csvFilePath = "path/malicious_url_data.csv";
            try (Reader reader = Files.newBufferedReader(Paths.get(csvFilePath));
                 CSVParser csvParser =
                         new CSVParser(
                                 reader,
                                 CSVFormat.DEFAULT
                                         .withHeader("url", "isMalicious")
                                         .withFirstRecordAsHeader()
                                         .withIgnoreHeaderCase()
                                         .withTrim())) {
                csvRecords = csvParser.getRecords();
            }
            return new CSVDataset(this);
        }
    }

}
