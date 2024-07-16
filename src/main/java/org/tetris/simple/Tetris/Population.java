package org.tetris.simple.Tetris;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**Manages the population of Tetris individuals. Each individual contains the logic to handle
 * the game as well as a neural network instance. The population class manages */
@Slf4j
public class Population {
    Tetris[] population;
    Tetris bestIndividual;

    int generation = 1;
    double best_fitness_score = 0;
    double totalFitnessScore = 0;
    Tetris best_individual;

    // generational stats
    int naturalDeaths = 0, timedDeaths = 0;

    // contains a list of all fitness values in the current population
    ArrayList<Double> fitnessScores = new ArrayList<>();
    HashMap<Double, Tetris> topTetris = new HashMap<>();

    JFrame frame;

    public Population(int size) {
        population = new Tetris[size];
        initPopulation();
    }

    public void setupFrame() {
        frame = new JFrame("TetrisAI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);
        frame.setVisible(true);

        try {
            var iconFilePath = new File("").getAbsolutePath().concat("\\src\\main\\resources\\images\\icon.png");
            frame.setIconImage(ImageIO.read(new File(iconFilePath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void initPopulation() {
        for (int i = 0; i < population.length; i++) {
            population[i] = new Tetris();
        }
        bestIndividual = population[0];
    }

    public void runCurrentPopulation() {
        Tetris currentIndividual;
        totalFitnessScore = 0;
        // lets each individual play until game over state is reached for final fitness score
        for (int id = 0; id < population.length; id++) {
            currentIndividual = population[id].playGame(id, generation, frame);

            if (currentIndividual.naturalDeath) naturalDeaths++;
            if (currentIndividual.timedDeath) timedDeaths++;

            totalFitnessScore += currentIndividual.well.fitness();

            final double currentFitnessScore = currentIndividual.well.fitness();
            fitnessScores.add(currentFitnessScore);
            // top 5 scores
            if (topTetris.size() < 5) {
                topTetris.put(currentFitnessScore, currentIndividual);
            } else if (topTetris.keySet().stream().anyMatch(x -> x < currentFitnessScore)) {
                double smallest = topTetris.keySet().stream().sorted().findFirst().orElse(0d);
                topTetris.remove(smallest);
                topTetris.put(currentFitnessScore, currentIndividual);
            }
            // fitness is currently total score + time alive
            if (currentFitnessScore > best_fitness_score) {
                best_fitness_score = currentFitnessScore;
                best_individual = currentIndividual;
            }
        }
    }

    private Tetris topFiveSelection() {
        var keyList = topTetris.keySet().stream().sorted().collect(Collectors.toList()).reversed();
        int key = ThreadLocalRandom.current().nextInt(0, 4);
        return topTetris.get(keyList.get(key)).clone();
    }

    // returns a random child according to the selection logic
    private Tetris topFiveCustomSelection() {
        double randomChance = 0.05d;
        if (Math.random() < randomChance) {
            int randomIndex = (int) (Math.random() * population.length);
            return population[randomIndex];
        } else {
            // TODO: Currently returns a random selection of the top 5 tetris
            var keyList = topTetris.keySet().stream().sorted().collect(Collectors.toList()).reversed();
            int key = ThreadLocalRandom.current().nextInt(0, 4);
            return topTetris.get(keyList.get(key)).clone();
        }
    }

    private Tetris fitnessSelection() {
        double rolledValue = ThreadLocalRandom.current().nextDouble(0, totalFitnessScore);
        double currentValue = 0;

        for (Tetris individual : population) {
            currentValue += individual.well.fitness();
            if (rolledValue <= currentValue) {
                return individual;
            }
        }

        // This should never happen
        return null;
    }

    public void populateNextGeneration() {
        Tetris[] newPopulation = new Tetris[population.length];
        this.generation++;
        newPopulation[0] = best_individual;
        newPopulation[0].generation = this.generation;
        for (int i = 1; i < population.length; i++) {
            // crosses over the respective neural networks
            Tetris parent1 = fitnessSelection();
            Tetris parent2 = fitnessSelection();
            Tetris child = parent1.crossover(parent2).clone();
            child.mutate();
            newPopulation[i] = child;
            newPopulation[i].id = population[i].id + population.length;
            newPopulation[i].generation = this.generation;
        }
        this.population = newPopulation;
        this.best_fitness_score = 0;
        this.fitnessScores.clear();
    }
}
