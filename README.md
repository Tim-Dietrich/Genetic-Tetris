# Genetic Tetris Project

This project aims to implement the genetic algorithm which is applied to a neural network that plays an instance of tetris.

![Tetris](https://i.imgur.com/Ldwi3Wd.gif)

*An individual "playing" the game.*

### Requirements

In order to build and run this yourself, you will need:

- [Java 21](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)
- [Gradle](https://gradle.org/)

*I will say sorry in advance, as the code is fairly ugly.*

# Idea

The aim of this project is to create a neural network configuration that is capable of playing Tetris to at least a fairly proficient level, keeping the game alive for quite some time and clearing at least 500 lines.

## Tetris

The underlying game logic is a modified version based on the work of Johannes Holzfuß: https://gist.github.com/DataWraith/5236083. In principle, it approximates the basic rules of tetris:

1. The playing field is a 10x20 board called the "well"
2. If a tetromino is placed, a new one will be picked and will join the board at the top
3. There are 7 possible tetrominos to chose from
   ![Tetrominos](https://i.imgur.com/hAfTYrr.png "Tetrominos")
4. Should the individual blocks of a tetromino fill an entire line in the well, score will be added to the player and the lines will be cleared

   1. The scoring logic is as follows:
      1. 1 line = 100 points
      2. 2 lines = 200 points
      3. 3 lines = 400 points
      4. 4 lines = 800 points
5. The game ends when a freshly spawned Tetromino can no longer move

## Evolutionary approach

In order to solve the problem of letting "an AI" play the game, an evolutionary approach using the genetic algorithm was chosen. The idea here is to let a neural network calculate scores based on every tetromino position available on the board. These scores are then used to select a "best" move and execute it, which will repeat until the player either finishes the game or times out based on a custom set condition.

A simple representation of how this approach works:
1. Generate a population of neural networks with random weights
2. Let the entire population play the game until everyone is finished
   1. For each individual, the neural network will calculate different scores for drop positions
   2. Scores are used to determine the next move in the game
3. Based on their fitness, select the best individuals and "breed" them
4. Repeat process

### The neural network

A primitive custom neural network was implemented. At its core, each individual will contain custom weightings for each edge that connects the nodes of the graph. The neural network parameters are customizable to some extent, with the current settings being as follows:

- 5 input nodes
- 4 hidden nodes per hidden layer
- 1 hidden layer
- 1 output node

*Example visualization of said network:*
![Neural Network](https://i.imgur.com/pI24kk0.png)

### Input value heuristics

To provide input values for the neural network, multiple different scoring heuristics are used to analyze each possible game-state:
1. The amount of lines cleared 
2. The amount of holes in the structure
3. The maximum height
4. The minimum height
5. The total difference between all columns of the structure in the well

(These are calculated for every possible drop position. Since there are 10 columns and each Tetromino has 4 possible rotations, this means there are 40 possible placements)

### Genetic Algorithm

#### Fitness

Fitness is calculated as such: `fitness = score + lifetime * 0.1`. Lifetime of an individual is included to make sure that longer-living individuals are still better than others even if they have the same score

#### Mutation

The mutation of individuals is executed on their edge weights $weight \in [-1,1]$ with a mutation rate of 0.05 (5%). The mutation itself is realized using a Gaussian distribution which is divided by 4. (More on this later)

#### Fitness Proportional Selection

After a population has finished playing, new parents are chosen according to a fitness proportional selection.

Additionally, the best individual of a generation will always be carried over to the next generation with no mutation.

#### Crossover

In order to realize a crossover between 2 parents, their 2D weight matrices need to be combined. For this, a random point will be picked for the rows and columns, essentially realizing a **one-point crossover**.

# Results

![Program in Progress](https://i.imgur.com/7NxsDrI.gif)

*An example of how the program looks during runtime.*

For data-collection, each run was executed using given parameters. A single run for 10000 tetris games will last approx. 6-10 minutes, depending on the parameters chosen. (Testing was done using an Intel-13700k processor)

The chosen result parameters I will look at are:
1. Average fitness of a generation
2. Highest fitness score of a given generation

*(Highscore plots will additionally receive a logarithmic trend-line)*

In the following I will include some observations based on some parameters and their influence on results.

If not otherwise specified, parameters are as follows:

- Mutation rate: 5%
- Population Size: 200
- Generations: 25
- Selection: Fitness-proportional selection
- Cross-over: One-point crossover

## Population Parameters

The interesting parameter here is the size of a given population. To achieve results in a timely manner, I limited each run to 5000 games:

(Population parameters tested are 50, 100, 200 and 500)

![Population Analysis](https://i.imgur.com/JyVeVFu.png)

The general conclusion for this parameter is that a generation setting around 200 is preferable.

I assume the following 2 conclusions:

- Too small population sizes do not introduce enough variance
- Too large population sizes depend less on the selection process and instead more on the mutation, taking longer to achieve similar results compared to smaller ones

## Mutation Rate

Here we look at how mutation rate affects the results:

(Mutation Parameters are 0.01, 0.05, 0.10 and 0.25)

![Mutation Rates](https://i.imgur.com/xznxf4B.png)

Interestingly enough, a "high" mutation rate works really well, even if the variance is higher as well. It could be assumed that this works well in combination with a larger population size as well as a fitness-proportional selection.

A "small" mutation rate will make slower progress towards an optimum but will then make steady improvements once close.

## Selection

I took an additional look at different types of selection for parents. Thus far only fitness-proportional selections were used. For this test I used the following functions:
1. fitness-proportional selection
2. rank selection (Top 5 individuals)
3. randomized rank selection (Top 5 individuals + chance for selection of random individual)

![Selection](https://i.imgur.com/ENiankK.png)

It is pretty clear that for this problem, fitness-proportional selection is a consistently better selection algorithm.

## Variance in runs

Warning: **Some** Results in this section are subject to relatively high variance. Due to the random generation at the start of a population, some runs will naturally produce better results than others. Due to time constraints, I could not do large amounts of runs to calculate better averages. Here is an example of this problem, utilizing the randomized rank selection:

![Variance Example](https://i.imgur.com/b8mvKUT.png)

Both Runs used the exact same parameters, but within 3 generations the difference in high score already has a factor of over 10.
Run 1 (blue) took ~3 minutes, Run 2 (gold) took ~7.5 minutes.

*I assume that this variance is caused by randomization factors. The parameters here use a custom selection function that includes a randomness factor, which may cause this variance.*

## Other interesting parameters

I'm not going into great detail here, but other parameters can also be modified for different results:

1. Neural network hidden layer parameters
   1. Higher hidden node count than 4 will lead to worse results
   2. A hidden node count of 3 seems to lead to the fastest results
   3. Changing the amount of hidden layers does not seem to lead to any interesting results
2. Scaling neural network input values (heuristic values) to a range of $x \in [0,1]$ does not lead to better results
3. When mutation of a matrix weight happens, a gaussian distribution is used
   1. It is beneficial to decrease the amount of the mutation by a factor of ~4
   2. No decrease will result in too much variance, leading to worse results
   3. Too high decrease will make the process take longer for proper results

# Conclusion

This approach will result in relatively good outcomes. It is possible to acquire neural network configurations that manage to play Tetris at a fairly proficient level, easily clearing more than 2000 lines in a single game.

There could be more optimization done to hand-select top individuals and breed these further, as that could result in better long-term results.
