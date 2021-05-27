package ua.kovalev.recommendation.mf.datastructure.matrix;


import ua.kovalev.recommendation.mf.datastructure.vector.SparseRealVector;

public class SparseRealMatrix implements SparseMatrix{

    private int rowCount;
    private int colCount;
    private SparseRealVector[] rows;
    private SparseRealVector[] cols;

    public SparseRealMatrix(int rowCount, int colCount){
        this.rowCount = rowCount;
        this.colCount = colCount;

        rows = new SparseRealVector[rowCount];
        cols = new SparseRealVector[colCount];

        for (int i = 0; i < rowCount; i++){
            rows[i] = new SparseRealVector(colCount);
        }
        for (int i = 0; i < colCount; i++){
            cols[i] = new SparseRealVector(rowCount);
        }
    }

    public SparseRealMatrix(SparseRealMatrix matrix){
        this.rowCount = matrix.rowCount;
        this.colCount = matrix.colCount;

        rows = new SparseRealVector[rowCount];
        cols = new SparseRealVector[colCount];

        for (int i = 0; i < rowCount; i++){
            rows[i] = matrix.getRow(i);
        }

        for (int i = 0; i < colCount; i++){
            cols[i] = matrix.getColumn(i);
        }
    }

    public SparseRealVector getRow(int row){
        return new SparseRealVector(getRowRef(row));
    }

    public SparseRealVector getColumn(int col){
        return new SparseRealVector(getColumnRef(col));
    }

    public SparseRealVector getRowRef(int row){
        validateRow(row);
        return rows[row];
    }

    public SparseRealVector getColumnRef(int col){
        validateColumn(col);
        return cols[col];
    }

    public SparseRealMatrix transpose(){
        SparseRealMatrix transposed = new SparseRealMatrix(this.colCount, this.rowCount);

        transposed.rows = cols;
        transposed.cols = rows;

        return transposed;
    }

    @Override
    public void setEntry(int row, int col, double value) {
        validatePosition(row, col);
        if (value == 0d){
            rows[row].removeEntry(col);
            cols[col].removeEntry(row);
        } else {
            rows[row].setEntry(col, value);
            cols[col].setEntry(row, value);
        }
    }

    @Override
    public double getEntry(int row, int col) {
        validatePosition(row, col);
        return rows[row].getEntry(col);
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    public int getEntryCount(){
        int totalSize = 0;
        for (SparseRealVector row : rows) {
            totalSize += row.getEntryCount();
        }
        return totalSize;
    }

    public void addRow(){
        addRow(new SparseRealVector(colCount));
    }

    public void addRow(SparseRealVector row){
        SparseRealVector[] newRows = new SparseRealVector[rowCount + 1];

        if (rowCount >= 0) {
            System.arraycopy(rows, 0, newRows, 0, rowCount);
        }
        newRows[rowCount] = row;

        for (int i = 0; i < colCount; i++){
            cols[i].incrementSize();
        }

        for (var entry : row){
            cols[entry.getKey()].setEntry(rowCount, entry.getValue());
        }

        rowCount++;
        rows = newRows;
    }

    private void validatePosition(int row, int col){
        validateRow(row);
        validateColumn(col);
    }

    private void validateRow(int row){
        if (row < 0 || row >= rowCount)
            throw new RuntimeException("Row " + row + " out of bounds [0, " + (rowCount - 1) + "]");
    }

    private void validateColumn(int col){
        if (col < 0 || col >= colCount)
            throw new RuntimeException("Column " + col + " out of bounds [0, " + (colCount - 1) + "]");
    }

    public void addColumn() {
        SparseRealVector[] newCols = new SparseRealVector[colCount + 1];

        if (colCount >= 0){
            System.arraycopy(cols, 0, newCols, 0, colCount);
        }
        newCols[colCount] = new SparseRealVector(rowCount);

        for (int i = 0; i < rowCount; i++){
            rows[i].incrementSize();
        }

        cols = newCols;
        colCount++;
    }
}
