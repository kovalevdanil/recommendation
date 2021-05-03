package ua.kovalev.recommendation.mf.algorithm.als;


import ua.kovalev.recommendation.mf.algorithm.Recommender;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.DenseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.util.MatrixUtils;
import ua.kovalev.recommendation.mf.util.VectorUtils;

import java.util.List;
import java.util.Map;

public class ModelALS extends Recommender {

    /**
     * Regularization parameter
     */
    private final double lambda;
    private final int factors;
    private final int maxIteration;
    private final int maxIterationsOnline;

    private final double latentInitMean;
    private final double latentInitDeviation;

    private DenseRealMatrix U;
    private DenseRealMatrix V;

    private SparseRealMatrix W;

    private double[] C;
    private final double w0;

    /**
     * Caches
     */
    DenseRealMatrix SU, SV;
    private double[] predictionItemsCache;
    private double[] predictionUsersCache;
    private double[] ratingItems;
    private double[] ratingUsers;
    private double[] wItems;
    private double[] wUsers;

    private final double newInteractionWeight = 0.1;

    public ModelALS(SparseRealMatrix trainMatrix, int topK, int threadNum,
                    int maxIteration, int factors, double lambda, double latentInitMean, double latentInitDeviation,
                    double alpha, double w0){
        super(trainMatrix,topK, threadNum);
        this.maxIteration = maxIteration;
        this.factors = factors;
        this.lambda = lambda;
        this.latentInitMean = latentInitMean;
        this.latentInitDeviation = latentInitDeviation;
        this.w0 = w0;
        this.maxIterationsOnline = 1;

        initWeightMatrix();
        initLatentMatrices();
        initPopularityVector(alpha, w0);
        initCache();
    }

    public ModelALS(SparseRealMatrix trainMatrix, Map<String, Object> config){
        super(trainMatrix, (int)config.getOrDefault(EALSConfig.TOP_K, 10), (int) config.getOrDefault(EALSConfig.THREAD_NUMBER, 1));

        this.maxIteration = (int)config.getOrDefault(EALSConfig.OFFLINE_ITERATIONS, 10);
        this.factors = (int) config.getOrDefault(EALSConfig.FACTORS, 16);
        this.lambda = (double) config.getOrDefault(EALSConfig.REGULARIZATION_PARAMETER, 0.01);
        this.latentInitDeviation = (double) config.getOrDefault(EALSConfig.LATENT_INIT_DEVIATION, 0.01);
        this.latentInitMean = (double) config.getOrDefault(EALSConfig.LATENT_INIT_MEAN, 0.01);
        this.w0 = (double) config.getOrDefault(EALSConfig.NEW_ITEM_WEIGHT, 1e-4);
        this.maxIterationsOnline = (int) config.getOrDefault(EALSConfig.ONLINE_ITERATIONS, 1);

        double alpha = (double) config.getOrDefault(EALSConfig.POPULARITY_SIGNIFICANCE, 0.4);

        initWeightMatrix();
        initLatentMatrices();
        initPopularityVector(alpha, w0);
        initCache();
    }

    private void initWeightMatrix(){
        this.W = new SparseRealMatrix(trainMatrix.getRowCount(), trainMatrix.getColumnCount());

        for (int u = 0; u < userCount; u++){
            for (int i : VectorUtils.getIndexList(trainMatrix.getRowRef(u))){
                W.setEntry(u, i, 1);
            }
        }
    }

    private void initLatentMatrices(){
        U = new DenseRealMatrix((int) userCount, factors);
        V = new DenseRealMatrix((int) itemCount, factors);

        U.init(latentInitMean, latentInitDeviation);
        V.init(latentInitMean, latentInitDeviation);
    }

    private void initPopularityVector(double alpha, double w0){
        C = createPopularityVector(alpha, w0);
    }

    private void initCache(){
        predictionUsersCache = new double[(int)userCount];
        predictionItemsCache = new double[(int)itemCount];

        ratingUsers = new double[(int) userCount];
        ratingItems = new double[(int) itemCount];

        wUsers = new double[(int) userCount];
        wItems = new double[(int) itemCount];

        SU = MatrixUtils.multiplyDense(U.transpose(), U);
        SV = new DenseRealMatrix(factors, factors);

        for (int f = 0; f < factors; f++){
            for (int k = 0; k <= f; k++){
                double val = 0;
                for (int i = 0; i < itemCount; i++){
                    val += V.getEntry(i, f) * V.getEntry(i, k) * C[i];
                }
                SV.setEntry(f, k, val);
                SV.setEntry(k, f, val);
            }
        }
    }

    private double[] createPopularityVector(double alpha, double w0){
        double sum = 0, Z = 0;
        double[] p = new double[(int)itemCount];
        for (int i = 0; i < itemCount; i ++) {
            p[i] = trainMatrix.getColumnRef(i).getEntryCount();
            sum += p[i];
        }
        for (int i = 0; i < itemCount; i ++) {
            p[i] /= sum;
            p[i] = Math.pow(p[i], alpha);
            Z += p[i];
        }
        double[] C = new double[(int)itemCount];
        for (int i = 0; i < itemCount; i ++)
            C[i] = w0 * p[i] / Z;
        return C;
    }

    @Override
    public double predict(int u, int i) {
        return U.getRow(u).multiply(V.getRow(i));
    }

    @Override
    public void buildModel() {
        for (int iteration = 0; iteration < maxIteration; iteration++){
            long start = System.currentTimeMillis();

            for (int u = 0; u < userCount; u++){
                updateUser(u);
            }

            for (int i = 0; i < itemCount; i++){
                updateItem(i);
            }

            System.out.printf("Iteration #%d, loss %f [%dl ms]%n", iteration + 1, loss(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public void updateModel(int u, int i) {
        trainMatrix.setEntry(u, i, 1);
        W.setEntry(u, i, newInteractionWeight);

        if (C[i] == 0){
            C[i] = w0 / itemCount;
            updateItemCache(i);
        }

        for (int j = 0; j < maxIterationsOnline; j++){
            updateUser(u);
            updateItem(i);
        }
    }

    public void updateUser(int u){
        List<Integer> itemList = VectorUtils.getIndexList(trainMatrix.getRowRef(u));

        if (itemList.size() == 0){
            return;
        }

        for (int i : itemList){
            predictionItemsCache[i] = predict(u, i);
            ratingItems[i] = trainMatrix.getEntry(u, i);
            wItems[i] = W.getEntry(u, i);
        }

        DenseRealVector oldUserLatentVector = U.getRow(u);

        for (int f = 0; f < factors; f++){
            double numerator = 0, denominator = 0;

            for (int k = 0; k < factors; k++){
                if (k != f){
                    numerator -= U.getEntry(u, k) * SV.getEntry(k, f);
                }
            }

            for (int i : itemList){
                predictionItemsCache[i] -= U.getEntry(u, f) * V.getEntry(i, f);
                numerator += (wItems[i] * ratingItems[i] - (wItems[i] - C[i]) * predictionItemsCache[i]) * V.getEntry(i, f);
                denominator += (wItems[i] - C[i]) * V.getEntry(i, f) * V.getEntry(i, f);
            }

            denominator += SV.getEntry(f, f) + lambda;

            U.setEntry(u, f, numerator / denominator);

            for (int i : itemList){
                predictionItemsCache[i] += U.getEntry(u, f) * V.getEntry(i, f);
            }
        }

        for (int f = 0; f < factors; f++){
            for (int k = 0; k <= f; k++){
                double val = SU.getEntry(f, k) - oldUserLatentVector.getEntry(f) * oldUserLatentVector.getEntry(k)
                        + U.getEntry(u, f) * U.getEntry(u, k);
                SU.setEntry(f, k, val);
                SU.setEntry(k, f, val);
            }
        }

    }

    public void updateItem(int i){
        List<Integer> userList = VectorUtils.getIndexList(trainMatrix.getColumnRef(i));
        if (userList.size() == 0){
            return;
        }

        for (int u : userList) {
            predictionUsersCache[u] = predict(u, i);
            ratingUsers[u] = trainMatrix.getEntry(u, i);
            wUsers[u] = W.getEntry(u, i);
        }

        DenseRealVector oldVector = V.getRow(i);

        for (int f = 0; f < factors; f++) {
            double numerator = 0, denominator = 0;
            for (int k = 0; k < factors;  k ++) {
                if (k != f)
                    numerator -= V.getEntry(i, k) * SU.getEntry(f, k);
            }
            numerator *= C[i];

            for (int u : userList) {
                predictionUsersCache[u] -= U.getEntry(u, f) * V.getEntry(i, f);
                numerator += (wUsers[u]*ratingUsers[u] - (wUsers[u]-C[i]) * predictionUsersCache[u]) * U.getEntry(u, f);
                denominator += (wUsers[u]-C[i]) * U.getEntry(u, f) * U.getEntry(u, f);
            }
            denominator += C[i] * SU.getEntry(f, f) + lambda;

            V.setEntry(i, f, numerator / denominator);

            for (int u : userList)
                predictionUsersCache[u] += U.getEntry(u, f) * V.getEntry(i, f);
        }

        for (int f = 0; f < factors; f ++) {
            for (int k = 0; k <= f; k ++) {
                double val = SV.getEntry(f, k) - oldVector.getEntry(f) * oldVector.getEntry(k) * C[i]
                        + V.getEntry(i, f) * V.getEntry(i, k) * C[i];
                SV.setEntry(f, k, val);
                SV.setEntry(k, f, val);
            }
        }
    }

    private void updateItemCache(int i){
        for (int f = 0; f < factors; f++){
            for (int k = 0; k <= f; k++){
                double val = SV.getEntry(f, k) + V.getEntry(i, f) * V.getEntry(i, k) * C[i];
                SV.setEntry(f, k, val);
                SV.setEntry(k, f, val);
            }
        }
    }


    private double loss(){
        double L = lambda * (MatrixUtils.squaredSum(U) + MatrixUtils.squaredSum(V));

        for (int u = 0; u < userCount; u ++) {
            double l = 0;
            for (int i : VectorUtils.getIndexList(trainMatrix.getRowRef(u))) {
                double pred = predict(u, i);
                l += W.getEntry(u, i) * Math.pow(trainMatrix.getEntry(u, i) - pred, 2);
                l -= C[i] * Math.pow(pred, 2);
            }
            l += SV.multiply(U.getRow(u)).dotProduct(U.getRow(u)).getEntry(0);
            L += l;
        }

        return L;
    }
}
