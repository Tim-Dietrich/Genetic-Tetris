package org.tetris.simple.Tetris;

import lombok.extern.slf4j.Slf4j;

import static org.tetris.simple.Utils.Config.*;

@Slf4j
public class GeneticTetris {

    public void runGeneticTetris() {
        Population population = new Population(populationSize);
        population.setupFrame();
        for (int i = 0; i < generationCount; i++) {
            population.naturalDeaths = 0;
            population.timedDeaths = 0;
            population.runCurrentPopulation();
            // Logging for evaluation
            double avgFitness = population.fitnessScores.stream().mapToDouble(a -> a).sum() / population.population.length;
            log.info("Best fitness for generation \t\t{}: player \t{} with \t{}", population.generation, population.best_individual.id, population.best_fitness_score);
            log.info("Average fitness for generation \t{}: {}", population.generation, avgFitness);
            log.info("Death distribution \tGen:{} \tnatural: {} \t timed: {}", population.generation, population.naturalDeaths, population.timedDeaths);
            population.populateNextGeneration();
        }
    }

}
