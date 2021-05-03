package ua.kovalev.recommendation.mf.datastructure.vector;

public interface RealVector {
    double getEntry(int i);
    void setEntry(int i, double num);
    RealVector dotProduct(RealVector vector);
    double multiply(RealVector vector);
    int getSize();
}
