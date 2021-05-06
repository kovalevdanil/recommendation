package ua.kovalev.recommendation.mf.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ArrayUtils {
    public static double[] copyAndIncrementSize(double[] array){
        double[] newArray = new double[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }
}
