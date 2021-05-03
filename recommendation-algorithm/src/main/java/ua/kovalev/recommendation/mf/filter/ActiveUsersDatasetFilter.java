package ua.kovalev.recommendation.mf.filter;

import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveUsersDatasetFilter implements DatasetFilter{

    private int minInteractionsNumber;

    public ActiveUsersDatasetFilter(int minInteractionsNumber){
        this.minInteractionsNumber = minInteractionsNumber;
    }

    @Override
    public void filter(Dataset dataset) {
        assert dataset != null;
        assert dataset.getRatings() != null;

        List<Rating> filteredRatings = null;
        List<Rating> ratings = dataset.getRatings();

        Map<Integer, Integer> userInteractions = new HashMap<>();

        for (Rating rating: ratings){
            userInteractions
                    .compute(rating.getUserId(), (k, v) -> (v == null ? 0 : v) + 1);
        }

        filteredRatings = ratings.stream()
                    .filter(rating -> userInteractions.get(rating.getUserId()) >= minInteractionsNumber)
                    .collect(Collectors.toList());

        int userCount = filteredRatings.stream().map(Rating::getUserId).max(Integer::compareTo).orElse(0);

        dataset.setUserCount(userCount);
        dataset.setRatings(filteredRatings);
    }
}
