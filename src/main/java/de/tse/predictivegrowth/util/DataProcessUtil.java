package de.tse.predictivegrowth.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.List;

public abstract class DataProcessUtil {

    public static Double getVarianceFromList(final List<Double> data) {
        return new Variance().evaluate(getPrimitiveArrayFromList(data));
    }

    public static Double getMeanFromList(final List<Double> data) {
        return new Mean().evaluate(getPrimitiveArrayFromList(data));
    }

    private static double[] getPrimitiveArrayFromList(final List<Double> listOfValues) {
        return ArrayUtils.toPrimitive(listOfValues.toArray(new Double[0]));
    }
}
