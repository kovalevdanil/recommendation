package ua.kovalev.recommendation.mf.util;

import lombok.experimental.UtilityClass;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;

@UtilityClass
public class DatasetUtils {

    public static SparseRealMatrix buildDatasetMatrix(Dataset dataset){
        SparseRealMatrix trainMatrix = new SparseRealMatrix(dataset.getUserCount(), dataset.getItemCount());

        for (Rating rating: dataset.getRatings()){
            trainMatrix.setEntry(rating.getUserId(), rating.getItemId(), 1);
        }

        return trainMatrix;
    }

}
