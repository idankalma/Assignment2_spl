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
        SharedVector[] vecsRows = new SharedVector[matrix.length];
        for(int i = 0; i < matrix.length; i++){
            vecsRows[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        acquireAllVectorWriteLocks(vectors);
        try{
            this.vectors = vecsRows;
        }
        finally {
            releaseAllVectorWriteLocks(vectors);
        }
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
        acquireAllVectorWriteLocks(vectors);
        try{
            this.vectors = vecsCols;
        }
        finally {
            releaseAllVectorWriteLocks(vectors);
        }
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (vectors.length == 0) {
            return new double[0][0];
        }
        int cols = vectors[0].length();
        int rows = vectors.length;
        double[][] output = new double[rows][cols];

        acquireAllVectorReadLocks(vectors);
        try {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    output[i][j] = vectors[i].get(j);
                }
            }
        }
        finally{
                releaseAllVectorReadLocks(vectors);
            }
            return output;
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
