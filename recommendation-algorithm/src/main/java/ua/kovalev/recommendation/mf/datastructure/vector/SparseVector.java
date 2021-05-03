package ua.kovalev.recommendation.mf.datastructure.vector;

import java.util.Iterator;
import java.util.Map;

public interface SparseVector extends RealVector{
    Iterator<Map.Entry<Integer, Double>> sparseIterator();
    void removeEntry(int index);
    int getEntryCount();
}
