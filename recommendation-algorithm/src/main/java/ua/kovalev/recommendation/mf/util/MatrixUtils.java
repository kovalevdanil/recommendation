package ua.kovalev.recommendation.mf.util;


import ua.kovalev.recommendation.mf.datastructure.matrix.DenseRealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.RealMatrix;
import ua.kovalev.recommendation.mf.datastructure.matrix.SparseRealMatrix;

public class MatrixUtils {
    public static SparseRealMatrix createSparseMatrix(int rows, int cols){
        return new SparseRealMatrix(rows, cols);
    }

    public static RealMatrix createDefaultMatrix(int rows, int cols){
        return new DenseRealMatrix(rows, cols);
    }

    public static SparseRealMatrix multiplySparse(SparseRealMatrix left, SparseRealMatrix right){
        if (!canBeMultiplied(left, right)) {
            throw new RuntimeException();
        }

        int rows = left.getRowCount(), cols = right.getColumnCount();

        SparseRealMatrix result = createSparseMatrix(rows, cols);

        multiply(left, right, result);

        return result;
    }

    public static DenseRealMatrix multiplyDense(DenseRealMatrix left, DenseRealMatrix right){
        if (!canBeMultiplied(left, right))
            throw new RuntimeException();

        int rows = left.getRowCount(), cols = right.getColumnCount();

        DenseRealMatrix result = new DenseRealMatrix(rows, cols);

        multiply(left, right, result);

        return result;
    }

    public static void multiply(RealMatrix left, RealMatrix right, RealMatrix result){
        if (!canBeMultiplied(left, right) || left.getRowCount() != result.getRowCount() || right.getColumnCount() != result.getColumnCount()){
            throw new RuntimeException();
        }

        int rows = result.getRowCount(), cols = result.getColumnCount();

        for (int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                double value = 0;

                for (int k = 0; k < left.getColumnCount(); k++) {
                    value += left.getEntry(i, k)  * right.getEntry(k, j);
                }

                if (value != 0d)
                    result.setEntry(i, j, value);
            }
        }
    }

    public static double squaredSum(DenseRealMatrix matrix){
        double sum = 0;
        for (int i = 0; i < matrix.getRowCount(); i++){
            for (int j = 0; j < matrix.getColumnCount(); j++){
                sum += matrix.getEntry(i,j) * matrix.getEntry(i, j);
            }
        }
        return sum;
    }


    public static boolean canBeMultiplied(RealMatrix left, RealMatrix right){
        return left.getColumnCount() == right.getRowCount();
    }

}
