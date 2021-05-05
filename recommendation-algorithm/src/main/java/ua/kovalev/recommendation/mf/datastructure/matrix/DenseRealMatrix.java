package ua.kovalev.recommendation.mf.datastructure.matrix;


import ua.kovalev.recommendation.mf.datastructure.vector.DenseRealVector;
import ua.kovalev.recommendation.mf.util.ArrayUtils;
import ua.kovalev.recommendation.mf.util.MathUtils;

import java.util.Arrays;

public class DenseRealMatrix implements RealMatrix{

    private int rowCount, colCount;
    private double[][] data;

    public DenseRealMatrix(int rowCount, int colCount){
        this.rowCount = rowCount;
        this.colCount = colCount;
        data = new double[rowCount][colCount];
    }

    public DenseRealMatrix(double[][] data){
        if (data.length == 0)
            throw new RuntimeException();

        rowCount = data.length;
        colCount = data[0].length;

        this.data = new double[rowCount][colCount];

        for (int i = 0; i < rowCount; i++)
            System.arraycopy(data[i], 0, this.data[i], 0, data[i].length);
    }

    public DenseRealMatrix(DenseRealMatrix matrix){
        this(matrix.data);
    }

    public DenseRealMatrix transpose(){
        DenseRealMatrix transposed = new DenseRealMatrix(colCount, rowCount);

        for (int i = 0; i < transposed.rowCount; i++){
            for (int j = 0; j < transposed.colCount; j++){
                transposed.setEntry(i, j, this.data[j][i]);
            }
        }

        return transposed;
    }

    @Override
    public void setEntry(int row, int col, double value) {
        validatePosition(row, col);
        data[row][col] = value;
    }

    @Override
    public double getEntry(int row, int col) {
        validatePosition(row, col);
        return data[row][col];
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    public DenseRealVector getRow(int row){
        validateRow(row);
        return new DenseRealVector(data[row]);
    }

    public void init(double mean, double sigma) {
        for (int i = 0; i < rowCount; i++)
            for (int j = 0; j < colCount; j++)
                data[i][j] = MathUtils.gaussian(0, mean, sigma);
    }

    public void setRow(int u, DenseRealVector newRow) {
        validateRow(u);

        if (newRow.getSize() != colCount){
            throw new RuntimeException("Incompatible sizes");
        }

        for (int i = 0; i < newRow.getSize(); i++){
            data[u][i] = newRow.getEntry(i);
        }
    }

    public double[] getRowRef(int u){
        validateRow(u);
        return data[u];
    }

    public DenseRealVector getColumn(int col){
        validateColumn(col);

        DenseRealVector vector = new DenseRealVector(rowCount);

        for (int i = 0; i < rowCount; i++)
            vector.setEntry(i, data[i][col]);

        return vector;
    }


    private void validatePosition(int row, int col){
        validateRow(row);
        validateColumn(col);
    }

    private void validateRow(int row){
        if (row < 0 || row >= rowCount)
            throw new RuntimeException();
    }

    private void validateColumn(int col){
        if (col < 0 || col >= colCount)
            throw new RuntimeException();
    }

    public DenseRealVector multiply(DenseRealVector vector) {
        if (getColumnCount() != vector.getSize()){
            throw new RuntimeException("Unable to multiply matrix " + rowCount + "x" + colCount + " on vector " + vector.getSize());
        }

        DenseRealVector result = new DenseRealVector(rowCount);
        for (int i = 0; i < rowCount; i++){
            result.setEntry(i, this.getRow(i).multiply(vector));
        }

        return result;
    }

    public void addRow(double[] row){
        if (row.length != colCount){
            throw new RuntimeException();
        }
        double[][] newData = new double[rowCount + 1][];
        if (rowCount >= 0) {
            System.arraycopy(data, 0, newData, 0, rowCount);
        }
        newData[rowCount] = row;

        rowCount++;
        data = newData;
    }

    public void addColumn(double[] col){
        if (col.length != rowCount){
            throw new RuntimeException();
        }

        for (int i = 0; i < rowCount; i++) {
            data[i] = ArrayUtils.copyAndIncrementSize(data[i]);
            data[i][colCount] = col[i];
        }

        colCount++;
    }

    public void addColumn(){
        for (int i = 0; i < rowCount; i++) {
            data[i] = ArrayUtils.copyAndIncrementSize(data[i]);
        }
        colCount++;
    }

    public void addRow(){
        addRow(new double[colCount]);
    }

    public void addRowInit(double mean, double sigma){
        double[] row = new double[colCount];
        for (int i = 0; i < colCount; i++){
            row[i] = MathUtils.gaussian(0, mean, sigma);
        }
        addRow(row);
    }

    public void add(int i, int j, double val){
        validatePosition(i, j);

        data[i][j] += val;
    }
}
