package de.tse.predictivegrowth.dataset;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.util.Progress;
import de.tse.predictivegrowth.model.StockDayData;
import lombok.NoArgsConstructor;

import java.util.List;

public class StockDataset extends RandomAccessDataset {

    @SuppressWarnings("FieldCanBeLocal")
    private final List<StockDayData> stockDayDataList;

    private StockDataset(Builder builder) {
        super(builder);
        this.stockDayDataList = builder.stockDayDataList;
    }

    @Override
    public Record get(NDManager manager, long index) {
        StockDayData record = this.stockDayDataList.get(Math.toIntExact(index));
        final NDArray value = manager.create(record.getPriceMean().floatValue());
        final NDArray label = manager.create(record.getLocalDate().toEpochDay());
        return new Record(new NDList(value), new NDList(label));
    }

    @Override
    public long availableSize() {
        return this.stockDayDataList.size();
    }

    @Override
    public void prepare(Progress progress) {}

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static final class Builder extends BaseBuilder<Builder> {

        private List<StockDayData> stockDayDataList;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setData(final List<StockDayData> stockDayDataList) {
            this.stockDayDataList = stockDayDataList;
            return this;
        }

        public StockDataset build() {
            return new StockDataset(this);
        }
    }
}
