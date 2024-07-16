package org.tetris.simple.Utils;

public class Config {
    // Game configuration variables
    public static final boolean reasonableSpeed = false;  // Use the gameSpeed globally
    public static final boolean spectatorMode = false;    // Use the gameSpeed only when an individual has reached a certain lifetime threshold

    public static final int gameSpeed = 10;               // Speed of the game (timeout in ms)
    public static final int timeOut = 1000000;            // Lifetime limit of an instance, after which it gets killed
    public static final int spectatorLimit = 30000;       // Lifetime threshold after which an individual can use the gameSpeed

    // Genetic Algorithm parameters
    public static final int populationSize = 200;
    public static final int generationCount = 20;
    public static final double mutationRate = 0.05d;
}
