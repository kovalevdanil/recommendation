package ua.kovalev.recommendation.mf.filter;

import ua.kovalev.recommendation.mf.data.Dataset;
import ua.kovalev.recommendation.mf.data.Interaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActiveUsersDatasetFilter implements DatasetFilter{

    private final int minInteractionsNumber;

    public ActiveUsersDatasetFilter(int minInteractionsNumber){
        this.minInteractionsNumber = minInteractionsNumber;
    }

    @Override
    public void filter(Dataset dataset) {
        assert dataset != null;
        assert dataset.getInteractions() != null;

        List<Interaction> filteredInteractions = null;
        List<Interaction> interactions = dataset.getInteractions();

        Map<Integer, Integer> userInteractions = new HashMap<>();

        for (Interaction interaction : interactions){
            userInteractions
                    .compute(interaction.getUserId(), (k, v) -> (v == null ? 0 : v) + 1);
        }

        filteredInteractions = interactions.stream()
                    .filter(inter -> userInteractions.get(inter.getUserId()) >= minInteractionsNumber)
                    .collect(Collectors.toList());

        int userCount = filteredInteractions.stream().map(Interaction::getUserId).max(Integer::compareTo).orElse(0);

        dataset.setUserCount(userCount);
        dataset.setInteractions(filteredInteractions);
    }
}
