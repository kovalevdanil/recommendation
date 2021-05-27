package ua.kovalev.recommendation.mf.util;


import lombok.experimental.UtilityClass;
import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.datastructure.vector.RealVector;
import ua.kovalev.recommendation.mf.datastructure.vector.SparseVector;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class VectorUtils {

    public static boolean sameSize(RealVector vector1, RealVector vector2){
        return vector1.getSize() == vector2.getSize();
    }

    public static List<Integer> getIndexList(SparseVector vector){
        List<Integer> result = new ArrayList<>();
        vector.sparseIterator().forEachRemaining(entry -> {
            result.add(entry.getKey());
        });
        return result;
    }

    public static double dotProduct(RealVector vector1, RealVector vector2){
        if (!sameSize(vector1, vector2)){
            throw new RuntimeException();
        }

        double result = 0;
        for (int i = 0; i < vector1.getSize(); i++){
            result += vector1.getEntry(i) * vector2.getEntry(i);
        }
        return result;
    }

    public static double norm(RealVector vector){
        double result = 0;
        for (int i = 0; i < vector.getSize(); i++){
            result += vector.getEntry(i) * vector.getEntry(i);
        }
        return Math.sqrt(result);
    }

    public static double cosine(DenseRealVector a, DenseRealVector b){
        double dotProduct = dotProduct(a, b);
        double aNorm = norm(a), bNorm = norm(b);
        return dotProduct / (aNorm * bNorm);
    }

    public static double dotProduct(double[] a, double[] b){
        if (a.length != b.length) {
            throw new RuntimeException("Vectors can't be different size");
        }

        int size = a.length;

        double result = 0;
        for (int i = 0; i < size; i++){
            result += a[i] * b[i];
        }
        return result;
    }

    public static double norm(double[] a){
        double result = 0;
        for (int i = 0; i < a.length; i++){
            result += a[i] * a[i];
        }
        return result;
    }

    public static double cosine(double[] a, double[] b){
        return dotProduct(a, b) / (norm(a) * norm(b));
    }

    public static double euclideanDistance(double[] a, double[] b){
        if (a.length != b.length){
            throw new RuntimeException("Vectors should be of same size");
        }

        int size = a.length;
        double sum = 0;

        for (int i = 0; i < size; i++){
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }
}
