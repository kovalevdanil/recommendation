package ua.kovalev.recommendation.mf.datastructure.matrix;

public interface RealMatrix {
    void setEntry(int row, int col, double value);
    double getEntry(int row, int col);
    int getRowCount();
    int getColumnCount();
}
