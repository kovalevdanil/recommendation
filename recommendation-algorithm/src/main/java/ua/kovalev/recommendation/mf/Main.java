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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {

    private static final int EXAMPLES_COUNT = 10;

    private static final int RATING_COUNT = 1_000_000;

    private static final int RECOMMEND_EXAMPLE_COUNT = 1000;

    private static final Map<String, Object> config = Map.of(
            EALSConfig.FACTORS, 32,
            EALSConfig.OFFLINE_ITERATIONS, 5,
            EALSConfig.REGULARIZATION_PARAMETER, 3d,
            EALSConfig.LATENT_INIT_DEVIATION, 0.5,
            EALSConfig.LATENT_INIT_MEAN, 0.01d,
            EALSConfig.POPULARITY_SIGNIFICANCE, 0.5d,
            EALSConfig.NEW_ITEM_WEIGHT, 1e-4,
            EALSConfig.TOP_K, 100
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
        SparseRealMatrix trainMatrix = buildTrainMatrix(data);
        System.out.println("Train Matrix is built in " + (System.currentTimeMillis() - startTimeMs) + " ms");

        EALSModel model = new EALSModel(trainMatrix, config);

        model.buildModel();

        double avgPredict = 0;
        double min = 1, max = 0;

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
            }
        }

        avgPredict /= trainMatrix.getRowCount() * trainMatrix.getColumnCount();
        System.out.println("Average prediction after " + config.get(EALSConfig.OFFLINE_ITERATIONS) + " iterations is " + avgPredict);
        System.out.println("Min prediction is " + min);
        System.out.println("Max prediction is " + max);


//        for (int i = 0; i < EXAMPLES_COUNT; i++){
//            Rating testRating = new Rating(new Random().nextInt(data.getUserCount()), new Random().nextInt(data.getItemCount()));
//
//            while (data.getRatings().contains(testRating)){
//                testRating = new Rating(new Random().nextInt(data.getUserCount()), new Random().nextInt(data.getItemCount()));
//            }
//
//            System.out.printf("Prediction for user %d and item %d is %f%n", testRating.getUserId(), testRating.getItemId(), model.predict(testRating.getUserId(), testRating.getItemId()));
//        }

//        int randomUserId = new Random().nextInt(data.getUserCount());
//
//        for (int i = 0; i < data.getItemCount(); i += 5){
//            System.out.printf("Prediction for user %d and item %d is %f%n", randomUserId, i, model.predict(randomUserId, i));
//        }

        startTimeMs = System.currentTimeMillis();
        List<Integer> items = model.getRecommendedItems(1, false);
        System.out.println("Recommendations generated in " + (System.currentTimeMillis() - startTimeMs) + " ms");
        System.out.println(items);

        EALSModel secondModel = new EALSModel(model.getTrainMatrix(), model.getU(), model.getV(), config);

        startTimeMs = System.currentTimeMillis();
        List<Integer> items2 = secondModel.getRecommendedItems(1, false);
        System.out.println("Recommendations in produced model generated in " + (System.currentTimeMillis() - startTimeMs) + " ms");
        System.out.println(items);
    }

    public static SparseRealMatrix buildTrainMatrix(Dataset dataset){
        SparseRealMatrix trainMatrix = new SparseRealMatrix(dataset.getUserCount() + 1, dataset.getItemCount() + 1);

        for (Rating rating: dataset.getRatings()){
            trainMatrix.setEntry(rating.getUserId(), rating.getItemId(), 1);
        }

        return trainMatrix;
    }
}