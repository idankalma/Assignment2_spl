import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import memory.SharedVector;
import memory.VectorOrientation;

public class SharedVectorTest {

    @Test
    public void testAddValid() {
        SharedVector v1 = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4,5,6}, VectorOrientation.ROW_MAJOR);

        v1.add(v2);

        assertArrayEquals(new double[]{5,7,9}, new double[]{
                v1.get(0), v1.get(1), v1.get(2)
        });
    }

    @Test
    public void testAddLengthMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    public void testAddOrientationMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1,2}, VectorOrientation.COLUMN_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    public void testNegate() {
        SharedVector v = new SharedVector(new double[]{1,-2,3}, VectorOrientation.ROW_MAJOR);
        v.negate();

        assertArrayEquals(new double[]{-1,2,-3},
                new double[]{v.get(0), v.get(1), v.get(2)});
    }

    @Test
    public void testDotProduct() {
        SharedVector row = new SharedVector(new double[]{1,2,3}, VectorOrientation.ROW_MAJOR);
        SharedVector col = new SharedVector(new double[]{4,5,6}, VectorOrientation.COLUMN_MAJOR);

        assertEquals(32, row.dot(col));
    }

    @Test
    public void testDotWrongOrientation() {
        SharedVector v1 = new SharedVector(new double[]{1,2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3,4}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> v1.dot(v2));
    }
}