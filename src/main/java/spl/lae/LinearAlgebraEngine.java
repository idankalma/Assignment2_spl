package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        // TODO: create executor with given thread count
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // TODO: resolve computation tree step by step until final matrix is produced
        computationRoot.associativeNesting();

        while (computationRoot.getNodeType() != ComputationNodeType.MATRIX) {
            ComputationNode node = computationRoot.findResolvable();
            if (node == null) {
                throw new IllegalStateException("Computation is not resolvable");
            }

            loadAndCompute(node);
        }
        try {
            executor.shutdown();
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        return computationRoot;
    }


    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
        List<ComputationNode> children = node.getChildren();
        ComputationNodeType type = node.getNodeType();

        if (type == ComputationNodeType.ADD || type == ComputationNodeType.MULTIPLY){
            if(children.size() < 2){
                throw new IllegalArgumentException("Multiply requires at least 2 operands");
            }
        }

        if(type == ComputationNodeType.NEGATE || type == ComputationNodeType.TRANSPOSE){
            if(children.size() != 1){
                throw new IllegalArgumentException("Unary operator requires exactly 1 operand");
            }
        }

        double[][] A = children.get(0).getMatrix();
        double[][] B = (children.size() > 1) ? children.get(1).getMatrix(): null;

        if (type == ComputationNodeType.ADD) {
            if (A.length != B.length || A[0].length != B[0].length) {
                throw new IllegalArgumentException("Dimension mismatch in matrix addition");
            }
        }


        if(type == ComputationNodeType.MULTIPLY){
            if(A[0].length != B.length){
                throw new IllegalArgumentException("Illegal operation: dimensions mismatch");
            }
        }

        synchronized (leftMatrix) {
            leftMatrix.loadRowMajor(A);
        }

        if (B != null) {
            synchronized (rightMatrix) {
                if (type == ComputationNodeType.MULTIPLY)
                    rightMatrix.loadColumnMajor(B);
                else
                    rightMatrix.loadRowMajor(B);
            }
        }

        List<Runnable> tasks;

        switch (type) {
            case ADD:
                tasks = createAddTasks();
                break;
            case MULTIPLY:
                tasks = createMultiplyTasks();
                break;
            case NEGATE:
                tasks = createNegateTasks();
                break;
            case TRANSPOSE:
                tasks = createTransposeTasks();
                break;
            default:
                throw new IllegalStateException("Unknown operation: " + node.getNodeType());
        }

        executor.submitAll(tasks);

        synchronized (leftMatrix) {
            node.resolve(leftMatrix.readRowMajor());
        }
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int row = i;
            tasks.add(() -> {
               SharedVector a = leftMatrix.get(row);
               SharedVector b = rightMatrix.get(row);

               a.add(b);
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int row = i;
            tasks.add(() -> {
                SharedVector rowVector = leftMatrix.get(row);
                rowVector.vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }


    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < leftMatrix.length(); i++) {
            final int row = i;
            tasks.add(() -> leftMatrix.get(row).negate());
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        int rows = leftMatrix.length();
        int cols = leftMatrix.get(0).length();

        rightMatrix.loadRowMajor(leftMatrix.readRowMajor());

        leftMatrix.loadRowMajor(new double[cols][rows]);

        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            final int row = i;
            tasks.add(() -> {
                    for(int j = 0; j < cols; j++){
                        double value = rightMatrix.get(row).get(j);
                        leftMatrix.get(j).set(row, value);
                }
            });
        }

        return tasks;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
    return executor.getWorkerReport();
    }
}
