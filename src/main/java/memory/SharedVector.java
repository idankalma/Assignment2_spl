package memory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        // TODO: return element at index (read-locked)
        readLock();
        try{
            return vector[index];
        }
        finally {
            readUnlock();
        }
    }

    public void set(int index, double newVal){
        writeLock();
        try{
            vector[index] = newVal;
        }
        finally {
            writeUnlock();
        }
    }

    public int length() {
        // TODO: return vector length
        return this.vector.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return vector orientation
        readLock();
        try {
            return this.orientation;
        }
        finally {
            readUnlock();
        }
    }

    public void writeLock() {
        // TODO: acquire write lock
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        // TODO: release write lock
        lock.writeLock().unlock();
    }

    public void readLock() {
        // TODO: acquire read lock
        lock.readLock().lock();
    }

    public void readUnlock() {
        // TODO: release read lock
        lock.readLock().unlock();
    }

    /**
     * Transposing the vector:
     * row -> column
     * column -> row
     */
    public void transpose() {
        // TODO: transpose vector
        writeLock();
        try{
            if(this.orientation == VectorOrientation.ROW_MAJOR){
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            }
            else if(this.orientation == VectorOrientation.COLUMN_MAJOR){
                this.orientation = VectorOrientation.ROW_MAJOR;
            }
        }
        finally {
            writeUnlock();
        }
    }

    /**
     * @param other
     * Add another vector to this vector (in-place).
     * Both vectors must have same length and orientation.
     */
    public void add(SharedVector other) {
        // TODO: add two vectors
        if (this.length() != other.length())
            throw new IllegalArgumentException("Vector length mismatch");

        if (this.orientation != other.orientation)
            throw new IllegalArgumentException("Vector orientation mismatch");

        SharedVector first = this;
        SharedVector second = other;

        if (System.identityHashCode(first) > System.identityHashCode(second)) {
            first = other;
            second = this;
        }

        first.writeLock();
        second.readLock();
        try{
            for(int i = 0; i < length(); i++){
                this.vector[i] = this.vector[i] + other.vector[i];
            }
        }
        finally {
            first.writeUnlock();
            second.readUnlock();
        }
    }

    /**
     * negate this vector
     */
    public void negate() {
        // TODO: negate vector
        writeLock();
        try{
            for(int i =0; i < length(); i++){
                this.vector[i] = -this.vector[i];
            }
        }
        finally {
            writeUnlock();
        }
    }

    /**
     *
     * @param other
     * @return sum = row(this) * column(other)
     */
    public double dot(SharedVector other) {
        // TODO: compute dot product (row · column)
        if (this.length() != other.length())
            throw new IllegalArgumentException("Vector length mismatch");

        if(this.orientation == null || other.orientation == null)
            throw new IllegalArgumentException("orientation cannot be null");
        if (this.orientation != VectorOrientation.ROW_MAJOR || other.orientation != VectorOrientation.COLUMN_MAJOR)
            throw new IllegalArgumentException("Dot product requires row * column");

        SharedVector first = this;
        SharedVector second = other;

        if (System.identityHashCode(first) > System.identityHashCode(second)) {
            first = other;
            second = this;
        }

        first.readLock();
        second.readLock();
        try{
            double sum = 0;
            for(int i = 0; i < length(); i++){
                sum = sum + this.vector[i]*other.vector[i];
            }
            return sum;
        }
        finally {
            first.readUnlock();
            second.readUnlock();
        }
    }

    /**
     * Compute row-vector × matrix (result stored in this vector).
     * This vector must be a row.
     * @param matrix
     */
    public void vecMatMul(SharedMatrix matrix) {
        // TODO: compute row-vector × matrix
        if(getOrientation() != VectorOrientation.ROW_MAJOR){
            throw new IllegalArgumentException("vecMatMul requires ROW vector");
        }

        if(matrix.getOrientation() == null){
            throw  new IllegalArgumentException("Empty matrix");
        }

        /*
        *implementation of matrix as row-major
        */
        if(matrix.getOrientation() == VectorOrientation.ROW_MAJOR){
            int cols = matrix.get(0).length();
            int rows = matrix.length();

            if(length() != rows){
                throw new IllegalArgumentException("Dimension mismatch in vecMatMul");
            }
            double[] output = new double[cols];

            SharedVector[] rowsVecs = new SharedVector[rows];
            for (int i = 0; i < rows; i++){
                rowsVecs[i] = matrix.get(i);
            }

            for(SharedVector v: rowsVecs) {
                v.readLock();
            }

            readLock();

            try {
                for (int i = 0; i < cols; i++) {
                    double sum = 0;
                    for (int k = 0; k < rows; k++) {
                        sum = sum + this.vector[k] * rowsVecs[k].vector[i];
                    }
                    output[i] = sum;
                }
            }
            finally {
                    readUnlock();
                    for(SharedVector v: rowsVecs) {
                        v.readUnlock();
                    }
                }


            writeLock();
            try{
                this.vector = output;
            }
            finally {
                writeUnlock();
            }
        }

        /*
         *implementation of matrix as column-major
         */
        else {
            int cols = matrix.length();
            int rows = matrix.get(0).length();

            if(length() != rows)
                throw new IllegalArgumentException("Dimension mismatch in vecMatMul");

            double[] output = new double[cols];

            readLock();
            try {
                for(int i = 0; i < cols; i++){
                    SharedVector vectorColumn = matrix.get(i);
                    output[i] = this.dot(vectorColumn);
                }
            }
            finally {
                readUnlock();
            }

            writeLock();
            try{
                this.vector = output;
            }
            finally {
                writeUnlock();
            }
        }
    }
}
