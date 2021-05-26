package ua.kovalev.recommendation.mf;

import ua.kovalev.recommendation.mf.algorithm.als.EALSModel;
import ua.kovalev.recommendation.mf.algorithm.als.config.EALSConfig;
import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.DatasetConstants;
import ua.kovalev.recommendation.mf.data.Interaction;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.vector.SparseRealVector;
import ua.kovalev.recommendation.mf.filter.ActiveUsersDatasetFilter;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;
import ua.kovalev.recommendation.mf.filter.ShrinkUsersDatasetFilter;
import ua.kovalev.recommendation.mf.reader.NetflixDatasetLoader;
import ua.kovalev.recommendation.mf.reader.DatasetLoader;
import ua.kovalev.recommendation.mf.util.DatasetUtils;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final int NEW_USER_NUMBER = 1000;
    private static final int NEW_ITEM_COUNT = 1000;

    private static final int RATING_COUNT = 2_000_000;


    private static final Map<String, Object> config = Map.of(
            EALSConfig.FACTORS, 32,
            EALSConfig.OFFLINE_ITERATIONS, 10,
            EALSConfig.REGULARIZATION_PARAMETER, 0.3d,
            EALSConfig.LATENT_INIT_DEVIATION, 0.01d,
            EALSConfig.LATENT_INIT_MEAN, 0.0d,
            EALSConfig.POPULARITY_SIGNIFICANCE, 0.5d,
            EALSConfig.MISSING_DATA_WEIGHT, 8d,
            EALSConfig.NEW_ITEM_WEIGHT, 4d
    );

    public static void freshModelTest(){
        Dataset dataset = Dataset.builder()
                .interactions(new ArrayList<>())
                .itemCount(0)
                .userCount(0)
                .build();

        SparseRealMatrix interactionMatrix = DatasetUtils.buildInteractionMatrix(dataset);

        EALSModel model = new EALSModel(interactionMatrix, config);

        for (int u = 0; u < NEW_USER_NUMBER; u++){
            model.addUser();
        }

        for (int i = 0; i < NEW_ITEM_COUNT; i++){
            model.addItem();
        }

        int targetUserId = 1;

        for (int i = 0; i < 10; i++){
            int ii = new Random().nextInt(NEW_ITEM_COUNT);
            model.updateModel(targetUserId, ii);
        }

        return;
    }

    public static void ndcgAndHr() throws Exception{

        DatasetFilter activeUsersFilter = new ActiveUsersDatasetFilter(3);
        DatasetFilter shrinkIdsFilter = new ShrinkUsersDatasetFilter();

        // read input data and perform filtering
        DatasetLoader reader = new NetflixDatasetLoader(DatasetConstants.NETLFIX_DATASET, Arrays.asList(activeUsersFilter, shrinkIdsFilter));

        long startTimeMs = System.currentTimeMillis();
        Dataset data = reader.load(RATING_COUNT);
        System.out.println("Load " + data.getInteractions().size() + " interactions in " + msPassed(startTimeMs));

        System.out.println("Constructing test dataset...");
        startTimeMs = System.currentTimeMillis();

        Map<Integer, Boolean> seenUsers = new HashMap<>();
        List<Interaction> testInteractions = new ArrayList<>();

        for (Interaction interaction : data.getInteractions()){
            if (seenUsers.get(interaction.getUserId()) == null){
                testInteractions.add(interaction);
                seenUsers.put(interaction.getUserId(), true);
            }
        }

        for (Interaction interaction : testInteractions){
            data.getInteractions().remove(interaction);
        }

        System.out.println("Test dataset constructed in " + msPassed(startTimeMs));

        startTimeMs = System.currentTimeMillis();

        SparseRealMatrix trainMatrix = DatasetUtils.buildInteractionMatrix(data);

        EALSModel model = new EALSModel(trainMatrix, config);

        model.buildModel();

        System.out.println("Model was built in " + msPassed(startTimeMs));

        int hits = 0;
        double ndcg = 0;

        double maxNcdg = 0;

        for (Interaction interaction : testInteractions){
            List<Integer> recs = model.getRecommendations(interaction.getUserId(), 10, false);

            if (recs.contains(interaction.getItemId())){
                hits++;

                int ind = recs.indexOf(interaction.getItemId());
                ndcg = 1 / log2(ind + 2);

                maxNcdg = Math.max(maxNcdg, ndcg);
            }
        }
        System.out.println("Total hits: " + hits);

        System.out.println("Total interactions: " + (trainMatrix.getEntryCount()));
        System.out.println("Total user count: " + (model.getUserCount()));
        System.out.println("Total item count: " + (model.getItemCount()));

        System.out.println("HR: " + ((double) hits / model.getUserCount()));
        System.out.println("NDCG: " + (ndcg / model.getUserCount()));

        System.out.println("MAX NCDG: " + maxNcdg);
    }

    public static void main(String[] args) throws Exception {
        ndcgAndHr();
    }

    public static long msPassed(long startMs){
        return System.currentTimeMillis() - startMs;
    }

    public static double log2(double val){
        return Math.log(val) / Math.log(2);
    }
}