package ua.kovalev.recommendation.mf.reader;


import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;
import ua.kovalev.recommendation.mf.filter.DatasetFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetflixDatasetLoader implements DatasetLoader {

    private final String datasetName;
    private List<? extends DatasetFilter> filters;

    public NetflixDatasetLoader(String datasetName){
        this(datasetName, null);
    }

    public NetflixDatasetLoader(String datasetName, List<? extends DatasetFilter> filters){
        this.datasetName = datasetName;
        this.filters = filters == null ? Collections.emptyList() : filters ;
    }


    @Override
    public Dataset load() throws IOException {
        return load(-1);
    }

    @Override
    public Dataset load(int maxRatingCount) throws IOException {
        List<Rating> ratings = new ArrayList<>();
        int userCount = 0, itemCount = 0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(getStream(datasetName)));

        String line;
        int currentItem = 0;
        int count = 0;

        while ((line = reader.readLine()) != null){
            if (line.matches("[0-9]*:")){
                currentItem = Integer.parseInt(line.substring(0, line.indexOf(':')));

                itemCount = Math.max(currentItem, itemCount);
            } else {
                String[] row = line.split(",");

                int userId = Integer.parseInt(row[0]);

                userCount = Math.max(userId, userCount);
                ratings.add(new Rating(userId, currentItem));

                count++;
            }

            if (maxRatingCount > 0 && count >= maxRatingCount){
                break;
            }
        }

        Dataset dataset = new Dataset(ratings, userCount + 1, itemCount + 1);

        filter(dataset);

        return dataset;
    }

    private void filter(Dataset dataset){
        for (DatasetFilter filter: filters){
            filter.filter(dataset);
        }
    }

    private boolean isFilterNeeded(){
        return filters != null;
    }

    public InputStream getStream(String inputFileName){
        return this.getClass().getClassLoader().getResourceAsStream(inputFileName);
    }
}
