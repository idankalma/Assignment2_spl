import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;

    @BeforeEach
    void setup() {
        engine = new LinearAlgebraEngine(4);
    }

    /* =========================
       ADD
       ========================= */

    @Test
    void testAddMatrices() {
        double[][] A = {{1, 2}, {3, 4}};
        double[][] B = {{5, 6}, {7, 8}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        double[][] out = engine.run(addNode).getMatrix();

        assertArrayEquals(new double[]{6, 8}, out[0]);
        assertArrayEquals(new double[]{10, 12}, out[1]);
    }

    @Test
    void testAddDimensionMismatch() {
        double[][] A = {{1, 2}};
        double[][] B = {{1, 2}, {3, 4}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        assertThrows(IllegalArgumentException.class, () -> engine.run(addNode));
    }

    /* =========================
       NEGATE
       ========================= */

    @Test
    void testNegateMatrix() {
        double[][] A = {{1, -2}, {-3, 4}};
        ComputationNode nodeA = new ComputationNode(A);

        ComputationNode negateNode = new ComputationNode(ComputationNodeType.NEGATE, List.of(nodeA));

        double[][] out = engine.run(negateNode).getMatrix();

        assertArrayEquals(new double[]{-1, 2}, out[0]);
        assertArrayEquals(new double[]{3, -4}, out[1]);
    }

    /* =========================
       MULTIPLY
       ========================= */

    @Test
    void testMultiplyMatrices() {
        double[][] A = {{1, 2}, {3, 4}};
        double[][] B = {{5, 6}, {7, 8}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);

        ComputationNode multiplyNode = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(nodeA, nodeB));

        double[][] out = engine.run(multiplyNode).getMatrix();

        assertArrayEquals(new double[]{19, 22}, out[0]);
        assertArrayEquals(new double[]{43, 50}, out[1]);
    }

    /* =========================
       TRANSPOSE
       ========================= */

    @Test
    void testTransposeMatrix() {
        double[][] A = {{1, 2, 3}, {4, 5, 6}};
        ComputationNode nodeA = new ComputationNode(A);

        ComputationNode transposeNode = new ComputationNode(ComputationNodeType.TRANSPOSE, List.of(nodeA));

        double[][] out = engine.run(transposeNode).getMatrix();

        assertEquals(3, out.length);
        assertEquals(2, out[0].length);

        assertArrayEquals(new double[]{1, 4}, out[0]);
        assertArrayEquals(new double[]{2, 5}, out[1]);
        assertArrayEquals(new double[]{3, 6}, out[2]);
    }

    /* =========================
       COMPOSITION / TREE
       ========================= */

    @Test
    void testNestedComputationTree() {
        double[][] A = {{1, 2}, {3, 4}};
        double[][] B = {{5, 6}, {7, 8}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));
        ComputationNode negateNode = new ComputationNode(ComputationNodeType.NEGATE, List.of(addNode));

        double[][] out = engine.run(negateNode).getMatrix();

        assertArrayEquals(new double[]{-6, -8}, out[0]);
        assertArrayEquals(new double[]{-10, -12}, out[1]);
    }

    /* =========================
       CONCURRENCY SANITY
       ========================= */

    @Test
    void testLargeParallelAddition() {
        int n = 100;
        double[][] A = new double[n][n];
        double[][] B = new double[n][n];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                A[i][j] = i;
                B[i][j] = j;
            }

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        double[][] out = engine.run(addNode).getMatrix();

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                assertEquals(i + j, out[i][j]);
    }

    /* =========================
       WORKER REPORT
       ========================= */

    @Test
    void testWorkerReportNotEmpty() {
        double[][] A = {{1, 2}};
        double[][] B = {{3, 4}};

        ComputationNode nodeA = new ComputationNode(A);
        ComputationNode nodeB = new ComputationNode(B);
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));

        engine.run(addNode);

        String report = engine.getWorkerReport();
        assertNotNull(report);
        assertFalse(report.isEmpty());
    }
}
