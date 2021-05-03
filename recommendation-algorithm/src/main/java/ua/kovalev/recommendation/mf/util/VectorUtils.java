package ua.kovalev.recommendation.mf.util;


import ua.kovalev.recommendation.mf.datastructure.vector.RealVector;
import ua.kovalev.recommendation.mf.datastructure.vector.SparseVector;

import java.util.ArrayList;
import java.util.List;

public class VectorUtils {

    private VectorUtils(){}

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
}
