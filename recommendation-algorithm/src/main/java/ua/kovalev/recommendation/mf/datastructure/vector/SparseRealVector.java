package ua.kovalev.recommendation.mf.datastructure.vector;


import ua.kovalev.recommendation.mf.util.VectorUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SparseRealVector implements SparseVector, Iterable<Map.Entry<Integer, Double>>{
    private Map<Integer, Double> dataMap = new HashMap<>();
    private final int size;

    public SparseRealVector(int size) {
        this.size = size;
    }

    public SparseRealVector(SparseRealVector vector) {
        this.size = vector.getSize();
        this.dataMap = new HashMap<>(vector.dataMap);
    }

    @Override
    public double getEntry(int i) {
        validateIndex(i);
        Double result = dataMap.get(i);
        if (result == null)
            return 0;
        return result;
    }

    @Override
    public void setEntry(int i, double num) {
        validateIndex(i);
        if (num == 0.0d){
            dataMap.remove(i);
        } else {
            dataMap.put(i, num);
        }
    }

    @Override
    public RealVector dotProduct(RealVector vector) {
        if (!VectorUtils.sameSize(this, vector))
            throw new RuntimeException();

        RealVector result = new SparseRealVector(size);
        for (int i = 0; i < size; i++) {
            result.setEntry(i, this.getEntry(i) * vector.getEntry(i));
        }
        return result;
    }

    @Override
    public double multiply(RealVector vector) {
        return 0;
    }

    @Override
    public int getSize() {
        return size;
    }

    /**
     * Iterator that iterates through all set entries
     * @return Iterator
     */
    @Override
    public Iterator<Map.Entry<Integer, Double>> sparseIterator() {
        return dataMap.entrySet().iterator();
    }

    @Override
    public void removeEntry(int index) {
        dataMap.remove(index);
    }

    @Override
    public int getEntryCount() {
        return dataMap.size();
    }

    private void validateIndex(int i){
        if (i < 0 || i >= size)
            throw new RuntimeException();
    }

    @Override
    public Iterator<Map.Entry<Integer, Double>> iterator() {
        return sparseIterator();
    }
}
