package ua.kovalev.recommendation.mf.datastructure.vector;


import ua.kovalev.recommendation.mf.util.VectorUtils;

public class DenseRealVector implements RealVector{
    private double[] data;
    private int size;

    public DenseRealVector(int size){
        this.size = size;
        data = new double[size];
    }

    public DenseRealVector(double[] vectorData){
        this.size = vectorData.length;
        data = new double[size];
        System.arraycopy(vectorData, 0, data, 0, vectorData.length);
    }

    public DenseRealVector(DenseRealVector vector){
        this(vector.data);
    }

    public double secondNormSquared(){
        double norm = 0;
        for (int i = 0; i < size; i++){
            norm += data[i] * data[i];
        }
        return norm;
    }

    @Override
    public double getEntry(int i) {
        validateIndex(i);
        return data[i];
    }

    @Override
    public void setEntry(int i, double num) {
        validateIndex(i);
        data[i] = num;
    }

    @Override
    public int getSize(){
        return size;
    }

    @Override
    public RealVector dotProduct(RealVector vector) {
        if (!VectorUtils.sameSize(this, vector))
            throw new RuntimeException();

        RealVector result = new DenseRealVector(size);
        for (int i = 0; i < size; i++) {
            result.setEntry(i, this.getEntry(i) * vector.getEntry(i));
        }
        return result;
    }

    @Override
    public double multiply(RealVector vector) {
        if (!VectorUtils.sameSize(this, vector))
            throw new RuntimeException();

        double result = 0;
        for (int i = 0; i < size; i++) {
            result += data[i] * vector.getEntry(i);
        }
        return result;
    }

    private void validateIndex(int i){
        if (i < 0 || i >= size)
            throw new RuntimeException();
    }
}
