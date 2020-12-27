package de.tse.predictivegrowth.dataset;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.util.Progress;
import de.tse.predictivegrowth.model.InOutData;
import lombok.NoArgsConstructor;

public class StockDataset extends RandomAccessDataset {

    @SuppressWarnings("FieldCanBeLocal")
    private final InOutData inOutData;

    private StockDataset(Builder builder) {
        super(builder);
        this.inOutData = builder.inOutData;
    }

    @Override
    public Record get(NDManager manager, long index) {
        NDList value = new NDList();
        NDList label = new NDList();
        final NDArray valueArray = this.inOutData.getInputs().get(Math.toIntExact(index));
        final NDArray labelArray = this.inOutData.getLabels().get(Math.toIntExact(index));
        value.add(valueArray);
        label.add(labelArray);
        value.attach(manager);
        label.attach(manager);
        return new Record(value, label);
    }
    // final NDArray inputs = this.ndManager.create(meanDataArrayInputs).reshape(new Shape(setSize, seriesSize));

    @Override
    public long availableSize() {
        return this.inOutData.getInputs().size(0);
    }

    @Override
    public void prepare(Progress progress) {}

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor
    public static final class Builder extends BaseBuilder<Builder> {

        private InOutData inOutData;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setData(final InOutData inOutData) {
            this.inOutData = inOutData;
            return this;
        }

        public StockDataset build() {
            return new StockDataset(this);
        }
    }
}
