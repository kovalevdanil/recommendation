package ua.kovalev.recommendation.mf.filter;


import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Rating;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShrinkUsersDatasetFilter implements DatasetFilter{
    @Override
    public void filter(Dataset dataset) {
        assert dataset != null;
        assert dataset.getRatings() != null;

        Map<Integer, Integer> newUserIds = new HashMap<>();
        List<Rating> ratings = dataset.getRatings();

        int lastUserId = 0;

        for (Rating rating: ratings) {
            if (!newUserIds.containsKey(rating.getUserId())){
                newUserIds.put(rating.getUserId(), lastUserId++);
            }
        }

        for (Rating rating : ratings){
            rating.setUserId(newUserIds.get(rating.getUserId()));
        }

        dataset.setUserCount(lastUserId);
    }
}
