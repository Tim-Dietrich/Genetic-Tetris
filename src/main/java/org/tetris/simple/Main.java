package org.tetris.simple;

import lombok.extern.slf4j.Slf4j;
import org.tetris.simple.Tetris.GeneticTetris;

@Slf4j
public class Main {
    public static void main(String[] args) {
        GeneticTetris application = new GeneticTetris();
        application.runGeneticTetris();
    }
}
