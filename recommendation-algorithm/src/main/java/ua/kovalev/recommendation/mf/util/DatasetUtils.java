package ua.kovalev.recommendation.mf.util;

import lombok.experimental.UtilityClass;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Interaction;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;

@UtilityClass
public class DatasetUtils {

    public static SparseRealMatrix buildDatasetMatrix(Dataset dataset){
        SparseRealMatrix trainMatrix = new SparseRealMatrix(dataset.getUserCount(), dataset.getItemCount());

        for (Interaction interaction : dataset.getInteractions()){
            trainMatrix.setEntry(interaction.getUserId(), interaction.getItemId(), 1);
        }

        return trainMatrix;
    }

}
