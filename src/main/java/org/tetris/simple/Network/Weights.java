package org.tetris.simple.Network;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class Weights {
    int rows, cols;
    // INFO: range 0.0 - 1.0
    double[][] weightMatrix;

    public Weights(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.weightMatrix = new double[rows][cols];
    }

    public Weights(double[][] matrix) {
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.weightMatrix = matrix;
    }

    // Used only for the input matrix, hence the column hard-limit at 1
    public Weights(double[] input) {
        this.rows = input.length;
        this.cols = 1;
        this.weightMatrix = new double[rows][cols];
        for (int i = 0; i < input.length; i++) {
            this.weightMatrix[i][0] = input[i];
        }
    }

    public Weights onePointCrossover(Weights parent) {
        Weights child = new Weights(rows, cols);

        int lowerRowBound = ThreadLocalRandom.current().nextInt(rows);
        int lowerColBound = ThreadLocalRandom.current().nextInt(cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if ((i < lowerRowBound) || (i == lowerRowBound) && (j <= lowerColBound)) {
                    child.weightMatrix[i][j] = weightMatrix[i][j];
                } else {
                    child.weightMatrix[i][j] = parent.weightMatrix[i][j];
                }
            }
        }

        return child;
    }

    public Weights dot(Weights weights) {
        Weights result = new Weights(rows, weights.cols);

        if (cols == weights.rows) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < weights.cols; j++) {
                    double sum = 0;
                    for (int k = 0; k < cols; k++) {
                        sum += weightMatrix[i][k] * weights.weightMatrix[k][j];
                    }
                    result.weightMatrix[i][j] = sum;
                }
            }
        }
        return result;
    }

    // activates the current matrix
    public Weights activate() {
        Weights activated = new Weights(rows, cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                activated.weightMatrix[i][j] = relu(weightMatrix[i][j]);
            }
        }

        return activated;
    }

    // randomize ALL weights
    public void randomizeWeights() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                weightMatrix[i][j] = ThreadLocalRandom.current().nextDouble(-1d, 1d);
            }
        }
    }

    // adds a bias weighting to a weight matrix
    public Weights addBiasNode() {
        Weights biasMatrix = new Weights(rows+1, 1);
        for (int i = 0; i < rows; i++) {
            biasMatrix.weightMatrix[i][0] = weightMatrix[i][0];
        }
        biasMatrix.weightMatrix[rows][0] = 1;
        return biasMatrix;
    }

    /** See source: <a href="https://en.wikipedia.org/wiki/Rectifier_(neural_networks)">RELU</a>*/
    public double relu(double x) {
        return Math.max(0, x);
    }

    /**Mutates the entire weight matrix with a given mutation rate.
     * @param mutationRate a given rate where 5% = 0.05*/
    public void mutateWeights(double mutationRate) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // from 0.0f to 1.0f
                double chance = ThreadLocalRandom.current().nextDouble();
                if (chance < mutationRate) {
                    // log.info("Muted weight {} - {}", i, j);
                    weightMatrix[i][j] += ThreadLocalRandom.current().nextGaussian() / 4;
                    // make sure not to exceed weight ranges
                    weightMatrix[i][j] = Math.clamp(weightMatrix[i][j], 0, 1);
                }
            }
        }
    }

    public double[] toArray() {
        double[] result = new double[rows*cols];
        for (int i = 0; i < rows; i++) {
            // WARNING: this was autocorrected by the IDE, I don't trust it
            if (cols >= 0) System.arraycopy(weightMatrix[i], 0, result, i * cols, cols);
        }
        return result;
    }

    // INFO: crossover implementations here

    @Override
    public Weights clone() {
        Weights clone = new Weights(rows, cols);
        for (int i = 0; i < rows; i++) {
            if (cols >= 0) System.arraycopy(weightMatrix[i], 0, clone.weightMatrix[i], 0, cols);
        }
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(String.valueOf(weightMatrix[i][j]), 0, 5).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
