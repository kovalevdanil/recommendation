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

    public double dotProduct(RealVector vector1, RealVector vector2){
        if (!sameSize(vector1, vector2)){
            throw new RuntimeException();
        }

        double result = 0;
        for (int i = 0; i < vector1.getSize(); i++){
            result += vector1.getEntry(i) * vector2.getEntry(i);
        }
        return result;
    }

    public double norm(RealVector vector){
        double result = 0;
        for (int i = 0; i < vector.getSize(); i++){
            result += vector.getEntry(i) * vector.getEntry(i);
        }
        return Math.sqrt(result);
    }

    public double cosine(DenseRealVector a, DenseRealVector b){
        double dorProduct = dotProduct(a, b);
        double aNorm = norm(a), bNorm = norm(b);
        return dorProduct / (aNorm * bNorm);
    }
}
