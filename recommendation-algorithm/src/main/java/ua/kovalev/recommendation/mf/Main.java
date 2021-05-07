package ua.kovalev.recommendation.mf;

import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.DatasetConstants;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.filter.ActiveUsersDatasetFilter;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;
import ua.kovalev.recommendation.mf.filter.ShrinkUsersDatasetFilter;
import ua.kovalev.recommendation.mf.reader.NetflixRatingReader;
import ua.kovalev.recommendation.mf.reader.RatingReader;
import ua.kovalev.recommendation.mf.util.DatasetUtils;
import ua.kovalev.recommendation.mf.util.VectorUtils;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final int NEW_USER_NUMBER = 1000;

    private static final int EXAMPLES_COUNT = 10;

    private static final int RATING_COUNT = 1_000_000;

    private static final int RECOMMEND_EXAMPLE_COUNT = 1000;

    private static final Map<String, Object> config = Map.of(
            EALSConfig.FACTORS, 32,
            EALSConfig.OFFLINE_ITERATIONS, 50,
            EALSConfig.REGULARIZATION_PARAMETER, 0.5d,
            EALSConfig.LATENT_INIT_DEVIATION, 0.5,
            EALSConfig.LATENT_INIT_MEAN, 0.01d,
            EALSConfig.POPULARITY_SIGNIFICANCE, 0.5d,
            EALSConfig.NEW_ITEM_WEIGHT, 1e-4,
            EALSConfig.TOP_K, 50
    );

    public static void main(String[] args) throws IOException {

        DatasetFilter activeUsersFilter = new ActiveUsersDatasetFilter(3);
        DatasetFilter shrinkIdsFilter = new ShrinkUsersDatasetFilter();

        // read input data and perform filtering
        RatingReader reader = new NetflixRatingReader(DatasetConstants.NETLFIX_DATASET, Arrays.asList(activeUsersFilter, shrinkIdsFilter));

        long startTimeMs = System.currentTimeMillis();
        Dataset data = reader.read(RATING_COUNT);

        System.out.println("Read in " + (System.currentTimeMillis() - startTimeMs) + " ms");
        System.out.println("User Count " + data.getUserCount());
        System.out.println("Item Count " + data.getItemCount());

        startTimeMs = System.currentTimeMillis();
        SparseRealMatrix trainMatrix = DatasetUtils.buildDatasetMatrix(data);
        System.out.println("Train Matrix is built in " + (System.currentTimeMillis() - startTimeMs) + " ms");

        EALSModel model = new EALSModel(trainMatrix, config);

        model.buildModel();

        double avgPredict = 0;
        double min = 1, max = 0;
        int countGreaterOne = 0, countLessZero = 0;

        for (int i = 0; i < trainMatrix.getRowCount(); i++){
            for (int j = 0; j < trainMatrix.getColumnCount(); j++){
                double currentPredict = model.predict(i, j);
                avgPredict += currentPredict;

                if (currentPredict > max){
                    max = currentPredict;
                }

                if (currentPredict < min){
                    min = currentPredict;
                }

                if (currentPredict > 1){
                    countGreaterOne++;
                }

                if (currentPredict < 0){
                    countLessZero++;
                }
            }
        }

        System.out.println("Total elements: " + (trainMatrix.getColumnCount() * trainMatrix.getRowCount()));
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Avg: " + avgPredict / (trainMatrix.getColumnCount() * trainMatrix.getRowCount()));
        System.out.println("Count Greater One: " + countGreaterOne);
        System.out.println("Count Less Zero: " + countLessZero);

        int randomUser;
        List<Integer> interactions;
        do {
            randomUser = new Random().nextInt(data.getUserCount());
            interactions = VectorUtils.getIndexList(model.getTrainMatrix().getRowRef(randomUser));
        } while (interactions.size() < 10);

        List<Integer> recommended = model.getRecommendations(randomUser, 50, false);

        for (int i : interactions){
            System.out.println("[REAL] User " + randomUser + ", item " + i + ", prediction " + model.predict(randomUser, i));
        }

        for (int i : recommended){
            System.out.println("[PREDICTED] User " + randomUser + ", item " + i + ", prediction " + model.predict(randomUser, i));
        }

        Set<Integer> intersection = new HashSet<>(interactions);

        intersection.retainAll(recommended);
        System.out.println("Predicted: "  + recommended);
        System.out.println("Real interactions: "  + interactions);
        System.out.println("Intersection: " + intersection);

    }
}