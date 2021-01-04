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

    public static float[] shiftArrayContentLeft(float[] nums) {
        if (nums == null || nums.length <= 1) {
            return nums;
        }
        float start = nums[0];
        System.arraycopy(nums, 1, nums, 0, nums.length - 1);
        nums[nums.length - 1] = start;
        return nums;
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
}
