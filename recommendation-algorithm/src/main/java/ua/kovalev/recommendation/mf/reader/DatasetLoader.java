package ua.kovalev.recommendation.mf.reader;


import ua.kovalev.recommendation.mf.data.Dataset;

import java.io.IOException;

public interface DatasetLoader {
    Dataset load() throws IOException;
    Dataset load(int maxInteractionCount) throws IOException;
}
