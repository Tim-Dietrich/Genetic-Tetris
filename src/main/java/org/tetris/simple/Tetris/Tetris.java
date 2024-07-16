package org.tetris.simple.Tetris;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tetris.simple.Network.NeuralNetwork;
import org.tetris.simple.Utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;

import static org.tetris.simple.Tetris.Tetrominos.tetrominos;
import static org.tetris.simple.Utils.Config.*;
import static org.tetris.simple.Utils.TetrisColors.*;

@Slf4j
public class Tetris extends JPanel {
    protected Well well;
    protected int id, generation;

    // game variables
    private final Color background = COLOR_1.color;
    private final Color cellBackground = COLOR_2.color;
    private int windowHeight;
    private int windowWidth;
    private final int border = 40;
    private final int cellDimension = 25;
    private boolean humanPlayer;

    // Neural Network
    NeuralNetwork neuralNetwork;

    // stats
    private int holes, minHeight, maxHeight, totalHeightDifference;
    protected boolean naturalDeath = false, timedDeath = false;

    // Creates a border around the well and initializes the dropping piece

    public void init(int width, int height, boolean humanPlayer) {
        configureFont();
        this.windowHeight = height;
        this.windowWidth = width;
        this.well = new Well(10, 20, cellBackground);
        this.well.newPiece();
        this.humanPlayer = humanPlayer;
    }

    public Tetris playGame(int id, int generation, JFrame frame) {
        setupTetris(id, generation);
        frame.add(this);

        // int currentPiece = -1;
        ArrayList<Integer> moves = new ArrayList<>();

        boolean optionalTrigger = false;

        // INFO: hard set limit at 15000 for now!
        while (!well.gameOver && well.lifeTime < timeOut) {
            if (spectatorMode && well.lifeTime > spectatorLimit) {
                optionalTrigger = true;
            }
            // 1. Create moves if the current piece is new
            if (well.newPiece && moves.isEmpty()) {
                well.newPiece = false;
                // currentPiece = well.currentPiece;
                moves = movesForBestPiece();
            }
            // 2. Execute moves from list
            if (!moves.isEmpty()) {
                movePiece(moves.getFirst());
                moves.removeFirst();
            }
            // 3. let the piece drop till it gets set
            update();

            repaint();
            calculateScores();

            if (reasonableSpeed || optionalTrigger) {
                try {
                    Thread.sleep(gameSpeed);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (well.gameOver) {
            naturalDeath = true;
        } else {
            timedDeath = true;
        }

        frame.remove(this);
        return this;
    }

    private void setupTetris(int id, int generation) {
        init(600, 700, false);
        // WARNING: only create new NN on 1st iteration, afterwards children are used
        if (generation == 1) {
            initNeuralNetwork(id, generation);
            neuralNetwork.rollInitialWeights();
        }
        setSize(600, 700);
        setVisible(true);
    }

    // Rules for hidden node count:
    // https://medium.com/geekculture/introduction-to-neural-network-2f8b8221fbd3#:~:text=Number%20of%20Neurons%20and%20Number%20of%20Layers%20in%20Hidden%20Layer&text=The%20number%20of%20hidden%20neurons,size%20of%20the%20output%20layer.
    public void initNeuralNetwork(int id, int generation) {
        if (!humanPlayer) {
            neuralNetwork = new NeuralNetwork(5, 4, 1, 1);
            this.id = id;
            this.generation = generation;
        }
    }

    protected void update() {
        well.dropDown();
        well.lifeTime++;
    }

    protected Tetris crossover(Tetris parent) {
        Tetris child = new Tetris();
        child.neuralNetwork = neuralNetwork.crossover(parent.neuralNetwork);
        return child;
    }

    protected void mutate() {
        neuralNetwork.mutate();
    }

    private void configureFont() {
        // try {
        //     var fontFilePath = new File("").getAbsolutePath().concat("\\src\\main\\resources\\fonts\\RubikMonoOne-Regular.ttf");
        //     File font_file = new File(fontFilePath);
        //     this.setFont(Font.createFont(Font.TRUETYPE_FONT, font_file).deriveFont(Font.PLAIN, 15f));
        // } catch (FontFormatException | IOException e) {
        //     throw new RuntimeException(e);
        // }
    }

    public Tetris clone() {
        Tetris clone = new Tetris();
        clone.init(this.windowWidth, this.windowHeight, this.humanPlayer);
        clone.neuralNetwork = neuralNetwork.clone();
        return clone;
    }

    // Draw the falling piece
    private void drawPiece(Graphics g) {
        g.setColor(well.tetrominoColors[well.currentPiece]);
        for (Point p : tetrominos[well.currentPiece][well.rotation]) {
            g.fillRect(border + (p.x + well.pieceOrigin.x) * 26,
                border + (p.y + well.pieceOrigin.y) * 26,
                cellDimension, cellDimension);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        // Paint the well
        g.setColor(background);
        g.fillRect(0, 0, windowWidth, windowHeight);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 20; j++) {
                g.setColor(well.well[i][j]);
                g.fillRect(border + 26*i, border + 26*j, cellDimension, cellDimension);
            }
        }

        // Display the score
        int pos = 35;
        g.setColor(COLOR_5.color);
        g.drawString("GEN: " + generation, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString("ID: " + id, border + cellDimension * well.wellWidth + 40, pos);
        pos += 30;
        g.drawString("Specs: ", border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" Life Time: " + well.lifeTime, border + cellDimension * well.wellWidth + 40, pos);
        pos += 30;
        g.drawString("Stats: ", border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" Score: " + well.score, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" Cleared: " + well.linesCleared, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" Holes: " + holes, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" MaxHeight: " + maxHeight, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" MinHeight: " + minHeight, border + cellDimension * well.wellWidth + 40, pos);
        pos += 15;
        g.drawString(" HeightDiff: " + totalHeightDifference, border + cellDimension * well.wellWidth + 40, pos);

        // Display status
        g.setColor(Color.RED);
        g.drawString(well.gameOver ? "Game Over!" : "", border + (cellDimension * well.wellWidth) / 2 - 40, 25);

        // Draw the currently falling piece
        drawPiece(g);
    }

    protected void calculateScores() {
        holes = well.countHoles();
        Tuple<Integer, Integer> minMaxTuple = well.getHeights();
        minHeight = minMaxTuple.getX();
        maxHeight = minMaxTuple.getY();
        totalHeightDifference = well.getTotalHeightDifferences();
    }

    protected void movePiece(int move) {
        switch (move) {
            // up
            case -1:
                well.rotate(-1);
                break;
            // down
            case 1:
                well.rotate(+1);
                break;
            // left
            case -2:
                well.move(-1);
                break;
            // right
            case 2:
                well.move(1);
                break;
            default:
                break;
        }
    }

    /* MOVE CALCULATION */

    public ArrayList<Integer> movesForBestPiece() {
        // 1. Create List of all valid positions in current board
        //      1.1 Create all starting positions consisting of x/y and rotation
        ListMultimap<Integer, Integer> validStartingPosition = ArrayListMultimap.create();
        // HashMap<Integer, Integer> validStartingPosition = new HashMap<>();
        for (int startingX = 0; startingX < well.wellWidth; startingX++) {
            for (int startingRot = 0; startingRot <= 3; startingRot++) {
                if (well.doesntCollideAt(startingX, 2, startingRot)) { // all pieces start dropping at y = 2
                    validStartingPosition.put(startingX, startingRot);
                }
            }
        }
        // log.info(validStartingPosition.toString());
        // 2. Score all collected positions according to NN
        // TODO: temporary highScore, not sure yet how else to resolve this
        // double highestScore = 100;
        double highestScore = -99999;
        Tuple<Integer, Integer> bestStarterPair = new Tuple<>(0, 0), currentPair = new Tuple<>(0, 0);

        ArrayList<Integer> bestMoveList = new ArrayList<>(), currentList = new ArrayList<>();

        HashMap<Double, Tuple<Integer, Integer>> completeList = new HashMap<>();

        //      2.1 create well duplicate and apply starter
        for (Integer startX : validStartingPosition.keySet()) {
            for (Integer startRot : validStartingPosition.get(startX)) {
                Well copy = well.copy();
                copy.currentPiece = well.currentPiece;
                copy.setNewPiece(startX, startRot);
                //      2.2 drop piece till collision
                while (copy.doesntCollideAt(copy.pieceOrigin.x, copy.pieceOrigin.y+1, startRot)) {
                    copy.dropDown();
                }
                copy.fixToWell();
                //      2.3 score well with dropped piece as set part
                // INFO: unscaled scores
                Tuple<Integer, Integer> minMaxTuple = copy.getHeights();
                double tHoles = copy.countHoles();
                double tMinHeight = minMaxTuple.getX();
                double tMaxHeight = minMaxTuple.getY();
                double linesDiff = copy.linesCleared - well.linesCleared;
                double copyLineHeightDiff = copy.getTotalHeightDifferences();
                double[] inputs = {tHoles, tMinHeight, tMaxHeight, copyLineHeightDiff, linesDiff};
                // double[] inputs = {copyLineHeightDiff, linesDiff, tMaxHeight, tMinHeight, tHoles};
                // double[] inputs = {linesDiff, tHoles, copyLineHeightDiff, tMaxHeight, tMinHeight};
                // INFO: Scaling of values to a range of 0.0 to 1.0
                double scaledHoles = tHoles / (17d * 10d); // 17 rows with 10 columns each
                double scaledMinHeight = tMinHeight / 18d;
                double scaledMaxHeight = tMaxHeight / 18d;
                double scaledLinesDiff = linesDiff / 4d;
                // Assumes that each line can reach up to 17 AND that there is a zigzag pattern filling the board
                double scaledTotalHeightDifference = copyLineHeightDiff / (18d * 9d);
                double[] scaledInputs = {scaledHoles, scaledMinHeight, scaledMaxHeight, scaledLinesDiff, scaledTotalHeightDifference};
                // var scoreDiff = copy.score - well.score;
                //      2.4 feed all scores into NN for evaluation
                // WARNING: temporary measure, the higher the worse the state is!
                // double evalScore = tHoles * 1.5 + Math.abs(tMaxHeight - tMinHeight) * 0.5 - scoreDiff * 0.1 + totalHeightDifference * 0.9;
                // WARNING: CURRENT NN RESULT
                // double evalScore = neuralNetwork.apply(scaledInputs)[0];
                double evalScore = neuralNetwork.calculateOutputLayer(inputs)[0];
                // log.info("eval score: {}", evalScore);
                // if (evalScore < highestScore) {

                // INFO for testing
                completeList.put(evalScore, new Tuple<>(startX, startRot));
                // log.info("Testing {} ({}) -> {}", new Tuple<>(startX, startRot), inputs, evalScore);

                if (evalScore == highestScore) {
                    currentPair = new Tuple<>(startX, startRot);
                    currentList = getMoveList(currentPair);
                    if (currentList.size() < bestMoveList.size()) {
                        bestMoveList = currentList;
                        highestScore = evalScore;
                        bestStarterPair = currentPair;
                    }
                } else if (evalScore > highestScore) {
                    currentPair = new Tuple<>(startX, startRot);
                    bestMoveList = getMoveList(currentPair);
                    highestScore = evalScore;
                    bestStarterPair = currentPair;
                }
            }
        }

        // log.info("-----------------------------------------------------------------");

        // 3. Create moves for best possible position
        //  = getMoveList(bestStarterPair);

        return bestMoveList;
    }

    private static ArrayList<Integer> getMoveList(Tuple<Integer, Integer> bestStarterPair) {
        ArrayList<Integer> moveList = new ArrayList<>();
        // INFO: everything < 5 needs to move left
        //  everything >= 5 needs to move right
        var requiredDirectionalMoves = bestStarterPair.getX() - 4;
        int moveType = requiredDirectionalMoves < 0 ? -2 : 2;

        // add rotational moves (first in order to avoid problems with the edge of the well)
        for (int i = 0; i < bestStarterPair.getY(); i++) {
            moveList.add(1);
        }

        // add directional moves
        for (int i = 0; i < Math.abs(requiredDirectionalMoves); i++) {
            moveList.add(moveType);
        }
        return moveList;
    }

    // @Override
    // public String toString() {
    //     StringBuilder sb = new StringBuilder();
    //     var t = neuralNetwork.toString();
    //     sb.append(t);
    //     return sb.toString();
    // }
}

