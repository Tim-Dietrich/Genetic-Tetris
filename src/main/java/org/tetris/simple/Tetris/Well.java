package org.tetris.simple.Tetris;

import org.tetris.simple.Utils.Tuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import static org.tetris.simple.Tetris.Tetrominos.tetrominos;
import static org.tetris.simple.Utils.TetrisColors.*;

public class Well {
    // Tetromino variables
    protected final Color[] tetrominoColors = {
            BLOCK_I.color,
            BLOCK_O.color,
            BLOCK_T.color,
            BLOCK_J.color,
            BLOCK_L.color,
            BLOCK_S.color,
            BLOCK_Z.color,
    };

    // Well variables
    protected Color[][] well;
    protected final int wellHeight;
    protected final int wellWidth;
    protected final Color cellBackground;

    // Piece variables
    protected Point pieceOrigin;
    protected int currentPiece;
    protected int rotation;
    protected final ArrayList<Integer> nextPieces = new ArrayList<>();

    // Game-state variables
    protected long score, linesCleared;
    protected boolean gameOver;
    protected int lifeTime;
    // used to tell the NN to generate a new set of moves when a new piece is chosen
    protected boolean newPiece = false;

    public Well(int wellWidth, int wellHeight, Color cellBackground) {
        this.well = new Color[wellWidth][wellHeight];
        this.cellBackground = cellBackground;
        this.wellHeight = wellHeight;
        this.wellWidth = wellWidth;
        // create an empty well
        for (int i = 0; i < wellWidth; i++) {
            for (int j = 0; j < wellHeight; j++) {
                well[i][j] = cellBackground;
                // testing with an almost filled line at the bottom
                // if (j >= wellHeight - 4 && i != 0) {
                //     well[i][j] = Color.gray;
                // }
            }
        }

    }

    public double fitness() {
        return score + lifeTime * 0.1f;
    }

    // copy existing well and its score
    public Well copy() {
        Well copy = new Well(wellWidth, wellHeight, cellBackground);
        for (int i = 0; i < wellWidth; i++) {
            if (wellHeight >= 0) System.arraycopy(well[i], 0, copy.well[i], 0, wellHeight);
        }
        copy.score = this.score;
        copy.linesCleared = this.linesCleared;
        return copy;
    }

    // Collision test for the dropping piece
    protected boolean doesntCollideAt(int x, int y, int rotation) {
        for (Point p : tetrominos[currentPiece][rotation]) {
            // 1st check well collision
            if (p.x + x < 0 || p.x + x >= wellWidth || p.y + y >= wellHeight) {
                return false;
            }
            // 2nd check tetromino collision
            if (well[p.x + x][p.y + y] != cellBackground) {
                return false;
            }
        }
        return true;
    }

    // Put a new, random piece into the dropping position
    public void newPiece() {
        pieceOrigin = new Point(4, 2);

        rotation = 0;
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6);
            // Collections.addAll(nextPieces, 0, 1, 0, 1, 0, 1);
            // Collections.addAll(nextPieces, 3, 4);
            Collections.shuffle(nextPieces);
        }
        currentPiece = nextPieces.getFirst();
        // currentPiece = 1;

        if (!doesntCollideAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
            gameOver = true;
            return;
        }
        this.newPiece = true;

        nextPieces.removeFirst();
    }

    public void setNewPiece(int x, int rotation) {
        this.pieceOrigin = new Point(x, 2);
        this.rotation = rotation;

        if (!doesntCollideAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
            gameOver = true;
        }
    }

    // Drops the piece one line or fixes it to the well if it can't drop
    public void dropDown() {
        if (pieceOrigin.y + 1 != wellHeight && doesntCollideAt(pieceOrigin.x, pieceOrigin.y + 1, rotation)) {
            pieceOrigin.y += 1;
        } else {
            fixToWell();
        }
    }

    // Rotate the piece clockwise or counterclockwise
    public void rotate(int i) {
        int newRotation = (rotation + i) % 4;
        if (newRotation < 0) {
            newRotation = 3;
        }
        if (doesntCollideAt(pieceOrigin.x, pieceOrigin.y, newRotation)) {
            rotation = newRotation;
        }
    }

    // Move the piece left or right
    public void move(int i) {
        if (doesntCollideAt(pieceOrigin.x + i, pieceOrigin.y, rotation)) {
            pieceOrigin.x += i;
        }
    }

    // Make the dropping piece part of the well, so it is available for
    // collision detection.
    public void fixToWell() {
        for (Point p : tetrominos[currentPiece][rotation]) {
            well[pieceOrigin.x + p.x][pieceOrigin.y + p.y] = tetrominoColors[currentPiece];
        }
        clearRows();
        newPiece();
    }

    public void deleteRow(int row) {
        for (int j = row-1; j > 0; j--) {
            for (int i = 0; i < 10; i++) {
                well[i][j+1] = well[i][j];
            }
        }
    }

    // Clear completed rows from the field and award score according to
    // the number of simultaneously cleared rows.
    public void clearRows() {
        boolean gap;
        int numClears = 0;

        for (int j = 19; j > 0; j--) {
            gap = false;
            for (int i = 0; i < 10; i++) {
                if (well[i][j] == cellBackground) {
                    gap = true;
                    break;
                }
            }
            if (!gap) {
                deleteRow(j);
                j += 1;
                numClears += 1;
            }
        }

        switch (numClears) {
            case 1:
                score += 100;
                break;
            case 2:
                score += 200;
                break;
            case 3:
                score += 400;
                break;
            case 4:
                score += 800;
                break;
        }

        linesCleared += numClears;
    }

    private int getColumnHeight(Color[] column) {
        int cur = 20;
        for (Color color : column) {
            if (color == cellBackground) {
                cur--;
            } else {
                break;
            }
        }
        return cur;
    }

    protected int countHoles() {
        int holes = 0;
        for (Color[] column : well) {
            boolean cellFound = false;
            for (Color color : column) {
                if (color != cellBackground && !cellFound) {
                    cellFound = true;
                } else if (color == cellBackground && cellFound) {
                    holes++;
                }
            }
        }
        return holes;
    }

    protected Tuple<Integer, Integer> getHeights() {
        ArrayList<Integer> cols = new ArrayList<>();
        for (Color[] column : well) {
            cols.add(getColumnHeight(column));
        }
        return new Tuple<>(Collections.min(cols), Collections.max(cols));
    }

    protected int getTotalHeightDifferences() {
        int curHeight, lastHeight = -1;
        int totalHeightDifference = 0;
        for (Color[] column : well) {
            curHeight = getColumnHeight(column);
            if (lastHeight != -1) {
                totalHeightDifference += Math.abs(lastHeight - curHeight);
            }
            lastHeight = curHeight;
        }
        return totalHeightDifference;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t--- Game state: ---\n");
        for (int i = 0; i < wellHeight; i++) {
            sb.append("\t");
            for (int j = 0; j < wellWidth; j++) {
                if (well[j][i] == cellBackground) {
                    sb.append("0 ");
                } else {
                    sb.append("1 ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
