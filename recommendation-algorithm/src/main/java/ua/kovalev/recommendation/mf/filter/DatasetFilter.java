package ua.kovalev.recommendation.mf.filter;


import ua.kovalev.recommendation.mf.data.Dataset;

public interface DatasetFilter {
    void filter(Dataset dataset);
}
