package ua.kovalev.recommendation.mf.algorithm;


import lombok.Getter;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;

import java.util.List;

public abstract class Recommender {
    @Getter
    protected SparseRealMatrix trainMatrix;

    protected int threadNumber;

    protected int topK;

    protected long userCount;
    protected long itemCount;


    public Recommender(SparseRealMatrix trainMatrix,  int topK, int threadNumber){
        this.trainMatrix = trainMatrix;
        this.threadNumber = threadNumber;
        this.topK = topK;

        this.userCount = trainMatrix.getRowCount();
        this.itemCount = trainMatrix.getColumnCount();
    }

    /**
     * Get the prediction score of user u on item i. To be overridden.
     */
    public abstract double predict(int u, int i);

    /**
     * Build the model.
     */
    public abstract void buildModel();

    /**
     * Update the model with a new observation.
     */
    public abstract void updateModel(int u, int i);

    /**
     * Get list of items user might like
     * @param u user id
     * @param excludeInteracted if true, then items that user interacted with are excluded from final list
     * @return list of item ids
     */
    public abstract List<Integer> getRecommendations(int u, int count, boolean excludeInteracted);
}
