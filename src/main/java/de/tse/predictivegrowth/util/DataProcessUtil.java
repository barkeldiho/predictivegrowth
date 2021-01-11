package de.tse.predictivegrowth.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.util.ArrayList;
import java.util.List;

public abstract class DataProcessUtil {

    public static Double getVarianceFromList(final List<Double> data) {
        return new Variance().evaluate(getPrimitiveArrayFromList(data));
    }

    public static Double getMeanFromList(final List<Double> data) {
        return new Mean().evaluate(getPrimitiveArrayFromList(data));
    }

    public static float[] shiftArrayContentLeft(float[] nums) {
        if (nums == null || nums.length <= 1) {
            return nums;
        }
        float start = nums[0];
        System.arraycopy(nums, 1, nums, 0, nums.length - 1);
        nums[nums.length - 1] = start;
        return nums;
    }

    public static List<Double> calcTlcc(final List<Double> actual, final List<Double> predicted) {
        if (actual.size() != predicted.size()) {
            throw new RuntimeException("List sizes do not match.");
        }

        final List<Double> shiftArray = new ArrayList<>(predicted);
        final List<Double> result = new ArrayList<>();
        for (int i = 0; i < actual.size(); i++) {
            final double ppmcc = new PearsonsCorrelation()
                    .correlation(DataProcessUtil.getPrimitiveArrayFromList(actual),DataProcessUtil.getPrimitiveArrayFromList(shiftArray));
            result.add(ppmcc);

            DataProcessUtil.shiftListLeft(shiftArray, 1);
            shiftArray.set(actual.size()-1, 0d);
        }
        return result;
    }

    private static <T> List<T> shiftListLeft(List<T> aL, int shift) {
        if (aL.size() == 0)
            return aL;

        T element = null;
        for(int i = 0; i < shift; i++) {
            element = aL.remove( 0);
            aL.add(element);
        }
        return aL;
    }

    private static double[] getPrimitiveArrayFromList(final List<Double> listOfValues) {
        return ArrayUtils.toPrimitive(listOfValues.toArray(new Double[0]));
    }

    public static Double getNormalizedValueForMinMax(final Double value, final Double max, final Double min) {
        return (value - min) / (max - min);
        // normalized_d = (d - min_d) / (max_d - min_d)
        // denormalized_d = normalized_d * (max_d - min_d) + min_d
    }

    public static Double getDenormalizedValueForMinMax(final Double value, final Double max, final Double min) {
        return value * (max - min) + min;
    }

    public static Double calcRSquared(final List<Double> actual, final List<Double> predicted) {
        final Double ssRes = DataProcessUtil.getResidualSumOfSquares(actual, predicted);
        final Double ssTot = DataProcessUtil.getTotalSumOfSquares(actual);
        return 1 - (ssRes/ssTot);
    }

    public static Double calcMeanAbsoluteError(final List<Double> actual, final List<Double> predicted) {
        if (actual.size() != predicted.size()) {
            throw new RuntimeException("List sizes do not match.");
        }

        double errorSum = 0d;
        for (int i = 0; i < actual.size(); i++) {
            errorSum += Math.abs(actual.get(i) - predicted.get(i));
        }
        return errorSum / actual.size();
    }

    public static Double calcRootMeanSquaredError(final List<Double> actual, final List<Double> predicted) {
        if (actual.size() != predicted.size()) {
            throw new RuntimeException("List sizes do not match.");
        }

        double errorSum = 0d;
        for (int i = 0; i < actual.size(); i++) {
            errorSum += Math.pow(actual.get(i) - predicted.get(i), 2);
        }
        return Math.sqrt(errorSum / actual.size());
    }

    private static Double getTotalSumOfSquares(final List<Double> data) {
        final Double mean = DataProcessUtil.getMeanFromList(data);
        return data.stream()
                .map(val -> Math.pow(val - mean, 2))
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private static Double getResidualSumOfSquares(final List<Double> actual, final List<Double> predicted) {
        if (actual.size() != predicted.size()) {
            throw new RuntimeException("List sizes do not match.");
        }

        final List<Double> result = new ArrayList<>();
        for (int i = 0; i < actual.size(); i++) {
            result.add(Math.pow(actual.get(i) - predicted.get(i), 2));
        }

        return result.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
