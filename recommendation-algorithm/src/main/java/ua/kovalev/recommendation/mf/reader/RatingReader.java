package ua.kovalev.recommendation.mf.reader;


import ua.kovalev.recommendation.mf.data.Dataset;

import java.io.IOException;

public interface RatingReader {
    Dataset read() throws IOException;
    Dataset read(int maxRatingCount) throws IOException;
}
