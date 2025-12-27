package memory;

import java.util.Vector;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        SharedVector[] newVectors = new SharedVector[matrix.length];
        for(int i = 0; i < matrix.length; i++){
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        int cols = matrix[0].length;
        int rows = matrix.length;

        SharedVector[] vecsCols = new SharedVector[cols];

        for (int j = 0; j < cols; j++) {
            double[] col = new double[rows];
            for (int i = 0; i < rows; i++) {
                col[i] = matrix[i][j];
            }
            vecsCols[j] = new SharedVector(col, VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = vecsCols;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (vectors.length == 0) {
            return new double[0][0];
        }

        if (getOrientation() == VectorOrientation.ROW_MAJOR) { // matrix is ROW_MAJOR
            int rows = vectors.length;
            int cols = vectors[0].length();
            double[][] output = new double[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    output[i][j] = vectors[i].get(j);
                }
            }
            return output;
        }
        else { // matrix is COLUMN_MAJOR
            int cols = vectors.length;
            int rows = vectors[0].length();
            double[][] output = new double[rows][cols];

            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < rows; i++) {
                    output[i][j] = vectors[j].get(i);
                }
            }
            return output;
        }
    }
    public SharedVector get(int index) {
        // TODO: return vector at index
        return vectors[index];
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if (vectors.length == 0) {
            return null;
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: acquire read lock for each vector
        for (SharedVector v : vecs) {
            v.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        // TODO: release read locks
        for (SharedVector v : vecs) {
            v.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: acquire write lock for each vector
        for (SharedVector v : vecs) {
            v.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        // TODO: release write locks
        for (SharedVector v : vecs) {
            v.writeUnlock();
        }
    }
}
