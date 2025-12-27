import memory.SharedMatrix;
import memory.SharedVector;
import memory.VectorOrientation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    /* Testing Constructors */

    @Test
    public void testEmptyMatrix() {
        // An empty constructor should create a matrix with no vectors
        SharedMatrix m = new SharedMatrix();

        // No rows or columns
        assertEquals(0, m.length());

        // Orientation of an empty matrix should be null
        assertNull(m.getOrientation());
    }

    @Test
    public void testConstructorRowMajor() {
        // Constructor with double[][] should load matrix as ROW_MAJOR
        double[][] data = {
                {1, 2},
                {3, 4}
        };

        SharedMatrix m = new SharedMatrix(data);

        // Two rows
        assertEquals(2, m.length());

        // Stored as row-major
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());
    }

    /* Testing loadRowMajor */

    @Test
    public void testLoadRowMajorAndRead() {
        // Loading a row-major matrix and reading it back
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadRowMajor(data);

        // readRowMajor should return the exact same matrix
        double[][] out = m.readRowMajor();
        assertArrayEquals(data, out);
    }

    @Test
    public void testLoadRowMajorOverridesOldMatrix() {
        // loadRowMajor should replace previous matrix content
        SharedMatrix m = new SharedMatrix();

        m.loadRowMajor(new double[][]{
                {1, 1},
                {1, 1}
        });

        m.loadRowMajor(new double[][]{
                {9, 9, 9}
        });

        double[][] out = m.readRowMajor();

        // New matrix should fully replace the old one
        assertEquals(1, out.length);
        assertEquals(3, out[0].length);
        assertEquals(9, out[0][0]);
    }

    /* Testing loadColumnMajor */

    @Test
    public void testLoadColumnMajorOrientation() {
        // Loading column-major should update orientation accordingly
        double[][] data = {
                {1, 2},
                {3, 4}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
    }

    @Test
    public void testLoadColumnMajorReadRowMajor() {
        // Even if stored as column-major, readRowMajor must return
        // a standard row-major double[][] representation
        double[][] data = {
                {1, 2},
                {3, 4}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        double[][] out = m.readRowMajor();
        assertArrayEquals(data, out);
    }

    /* Testing get */

    @Test
    public void testGetReturnsCorrectVector() {
        // get(index) should return a valid SharedVector
        double[][] data = {
                {7, 8, 9}
        };

        SharedMatrix m = new SharedMatrix(data);
        SharedVector v = m.get(0);

        assertNotNull(v);
        assertEquals(3, v.length());
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    public void testLengthAfterColumnMajorLoad() {
        // In column-major representation, length() equals number of columns
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        assertEquals(3, m.length());
    }

    /* Testing readRowMajor edge cases */

    @Test
    public void testReadRowMajorOnEmptyMatrix() {
        // Reading from an empty matrix should not throw an exception
        SharedMatrix m = new SharedMatrix();

        double[][] out = m.readRowMajor();
        assertEquals(0, out.length);
    }

    @Test
    public void testReadRowMajorNonSquareMatrix() {
        // Non-square matrix (2x3) should be handled correctly
        double[][] data = {
                {1, 2, 3},
                {4, 5, 6}
        };

        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);

        double[][] out = m.readRowMajor();

        assertEquals(2, out.length);
        assertEquals(3, out[0].length);
        assertEquals(6, out[1][2]);
    }

    /* Testing Integration with SharedVector */

    @Test
    public void testVectorModificationReflectsInMatrix() {
        // Modifying a SharedVector should be reflected
        // when reading the matrix
        double[][] data = {
                {1, 2}
        };

        SharedMatrix m = new SharedMatrix(data);
        SharedVector v = m.get(0);

        v.set(1, 42);

        double[][] out = m.readRowMajor();
        assertEquals(42, out[0][1]);
    }

    @Test
    public void testMultipleReadsAreConsistent() {
        // Multiple reads should produce the same result
        double[][] data = {
                {3, 4},
                {5, 6}
        };

        SharedMatrix m = new SharedMatrix(data);

        double[][] r1 = m.readRowMajor();
        double[][] r2 = m.readRowMajor();

        assertArrayEquals(r1, r2);
    }
}