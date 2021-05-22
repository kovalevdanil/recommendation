package ua.kovalev.recommendation.mf.filter;


import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Interaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShrinkUsersDatasetFilter implements DatasetFilter{
    @Override
    public void filter(Dataset dataset) {
        assert dataset != null;
        assert dataset.getInteractions() != null;

        Map<Integer, Integer> newUserIds = new HashMap<>();
        List<Interaction> interactions = dataset.getInteractions();

        int lastUserId = 0;

        for (Interaction interaction : interactions) {
            if (!newUserIds.containsKey(interaction.getUserId())){
                newUserIds.put(interaction.getUserId(), lastUserId++);
            }
        }

        for (Interaction interaction : interactions){
            interaction.setUserId(newUserIds.get(interaction.getUserId()));
        }

        dataset.setUserCount(lastUserId);
    }
}
