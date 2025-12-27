import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import memory.SharedVector;
import memory.SharedMatrix;
import memory.VectorOrientation;

public class SharedVectorTest {

    /* Testing Construction */

    /** Test that the length of a regular vector is returned correctly. */
    @Test
    public void testLength() {
        SharedVector v = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        assertEquals(3, v.length());
    }

    /** Test that an empty vector has length 0. */
    @Test
    public void testEmptyVector() {
        SharedVector v = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        assertEquals(0, v.length());
    }

    /* Testing get and set */

    /** Test that set correctly updates a value and get returns it. */
    @Test
    public void testGetSet() {
        SharedVector v = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        v.set(1, 10.5);
        assertEquals(10.5, v.get(1));
    }

    /* Testing Add */

    /** Test adding two vectors of same length and orientation. */
    @Test
    public void testAddValid() {
        SharedVector v1 = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4,5,6}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertArrayEquals(
                new double[]{5,7,9},
                new double[]{v1.get(0), v1.get(1), v1.get(2)}
        );
    }

    /** Test that adding vectors of different lengths throws an exception. */
    @Test
    public void testAddLengthMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    /** Test that adding vectors with different orientations throws an exception. */
    @Test
    public void testAddOrientationMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1,2}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    /* Testing Negate */

    /** Test that negate correctly flips the sign of all elements. */
    @Test
    public void testNegate() {
        SharedVector v = new SharedVector(new double[]{1,-2,3}, VectorOrientation.ROW_MAJOR);
        v.negate();
        assertArrayEquals(
                new double[]{-1,2,-3},
                new double[]{v.get(0), v.get(1), v.get(2)}
        );
    }

    /** Test that negating twice restores the original vector. */
    @Test
    public void testDoubleNegate() {
        SharedVector v = new SharedVector(new double[]{1,-2,3}, VectorOrientation.ROW_MAJOR);
        v.negate();
        v.negate();
        assertArrayEquals(
                new double[]{1,-2,3},
                new double[]{v.get(0), v.get(1), v.get(2)}
        );
    }

    /* Testing Transpose */

    /** Test that transposing a ROW vector turns it into a COLUMN vector. */
    @Test
    public void testTransposeRowToColumn() {
        SharedVector v = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }

    /** Test that transposing twice restores the original orientation. */
    @Test
    public void testTransposeTwiceReturnsOriginal() {
        SharedVector v = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        v.transpose();
        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    /* Testing Dot */

    /** Test dot product between ROW and COLUMN vector returns correct value. */
    @Test
    public void testDotProductValid() {
        SharedVector row = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{4,5,6}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(32, row.dot(col)); // 1*4 + 2*5 + 3*6
    }

    /** Test that dot product of same-orientation vectors throws an exception. */
    @Test
    public void testDotWrongOrientation() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3,4}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
    }

    /** Test that dot product of different-length vectors throws an exception. */
    @Test
    public void testDotLengthMismatch() {
        SharedVector row = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{3,4,5}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> row.dot(col));
    }

    /* Testing vecMatMul */

    /** Test vector-matrix multiplication with a COLUMN_MAJOR matrix. */
    @Test
    public void testVecMatMulColumnMajor() {
        SharedVector row = new SharedVector(
                new double[]{1,2},
                VectorOrientation.ROW_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
                {3,4},
                {5,6}
        });

        row.vecMatMul(m);

        assertArrayEquals(
                new double[]{13,16}, // 1*3 + 2*5, 1*4 + 2*6
                new double[]{row.get(0), row.get(1)}
        );
    }

    /** Test vector-matrix multiplication with a ROW_MAJOR matrix. */
    @Test
    public void testVecMatMulRowMajor() {
        SharedVector row = new SharedVector(
                new double[]{1,2},
                VectorOrientation.ROW_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[][]{
                {3,4},
                {5,6}
        });

        row.vecMatMul(m);

        assertArrayEquals(
                new double[]{13,16},
                new double[]{row.get(0), row.get(1)}
        );
    }

    /** Test that vecMatMul throws exception if the vector is COLUMN_MAJOR. */
    @Test
    public void testVecMatMulWrongOrientation() {
        SharedVector col = new SharedVector(
                new double[]{1,2},
                VectorOrientation.COLUMN_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[][]{
                {1,2},
                {3,4}
        });

        assertThrows(IllegalArgumentException.class, () -> col.vecMatMul(m));
    }

    /** Test that vecMatMul throws exception when vector and matrix dimensions mismatch. */
    @Test
    public void testVecMatMulDimensionMismatch() {
        SharedVector row = new SharedVector(
                new double[]{1,2,3},
                VectorOrientation.ROW_MAJOR
        );

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(new double[][]{
                {1,2},
                {3,4}
        });

        assertThrows(IllegalArgumentException.class, () -> row.vecMatMul(m));
    }

    /**
     * Test multiplying a row vector by an empty matrix.
     * Expected behavior: should throw IllegalArgumentException due to null orientation.
     */
    @Test
    public void testVecMatMulEmptyMatrix() {
        SharedVector row = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedMatrix m = new SharedMatrix(); // empty matrix

        assertThrows(IllegalArgumentException.class, () -> row.vecMatMul(m));
    }

    /**
     * Test multiplying a 1-element row vector by a 1x1 matrix.
     * Expected behavior: normal multiplication should work and return correct result.
     */
    @Test
    public void testVecMatMulOneElement() {
        SharedVector row = new SharedVector(new double[]{5}, VectorOrientation.ROW_MAJOR);

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(new double[][]{{3}}); // 1x1 matrix

        row.vecMatMul(m);

        assertArrayEquals(new double[]{15}, new double[]{row.get(0)});
    }
}
