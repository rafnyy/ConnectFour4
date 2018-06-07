package com._98point6.droptoken.board;

import com._98point6.droptoken.Constants;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WinEvaluator {
    private GameState gameState;
    private int currentRow;
    private int currentColumn;

    private Set<MatchedSpace> visted;

    public WinEvaluator(GameState gameState) {
        this.gameState = gameState;
        currentRow = 0;
        currentColumn = 0;
        visted = new HashSet<>();
    }

    private Space getSpaceAt(int rowIndex, int colIndex) {
        return gameState.getSpaces()[colIndex].getSpaces()[rowIndex];
    }

    public String whoWon() {
        do {
            Space space = getSpaceAt(currentRow, currentColumn);

            if(space != null && space.getWhichPlayer() != null) {
                Set<MatchedSpace> matchedSpaces = new HashSet<>();
                matchedSpaces.add(new MatchedSpace(space.getWhichPlayer(), currentRow, currentColumn, 1, DIR.UP));
                matchedSpaces.add(new MatchedSpace(space.getWhichPlayer(), currentRow, currentColumn, 1, DIR.RIGHT));
                matchedSpaces.add(new MatchedSpace(space.getWhichPlayer(), currentRow, currentColumn, 1, DIR.DIAGONAL));
                matchedSpaces.add(new MatchedSpace(space.getWhichPlayer(), currentRow, currentColumn, 1, DIR.BACK_DIAGONAL));

                for (MatchedSpace matchedSpace : matchedSpaces) {
                    if (checkNext(matchedSpace) != null) {
                        return matchedSpace.getPlayerId();
                    }
                }
            }

            moveToNextIndex();
        } while (currentRow < getNumRows() && currentColumn < getNumCols());

        return null;
    }

    private void moveToNextIndex() {
        if (currentColumn < getNumCols() - 1) {
            currentColumn++;
        } else {
            currentColumn = 0;
            currentRow++;
        }
    }

    private int getNumRows() {
        return gameState.getSpaces()[0].getSpaces().length;
    }

    private int getNumCols() {
        return gameState.getSpaces().length;
    }

    private MatchedSpace checkNext(MatchedSpace matchedSpace) {
        if (matchedSpace.getNumInARow() == Constants.winSize) {
            return matchedSpace;
        }

        if(visted.contains(matchedSpace)) {
            return null;
        }

        visted.add(matchedSpace);

        int nextRow;
        int nextCol;

        switch (matchedSpace.getDir()) {
            case UP:
                nextRow = matchedSpace.getRowIndex() + 1;
                nextCol = matchedSpace.getColIndex();
                break;
            case RIGHT:
                nextRow = matchedSpace.getRowIndex();
                nextCol = matchedSpace.getColIndex() + 1;
                break;
            case DIAGONAL:
                nextRow = matchedSpace.getRowIndex() + 1;
                nextCol = matchedSpace.getColIndex() + 1;
                break;
            case BACK_DIAGONAL:
                nextRow = matchedSpace.getRowIndex() + 1;
                nextCol = matchedSpace.getColIndex() - 1;
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (nextRow >= getNumRows() || nextCol >= getNumCols() || nextCol < 0) {
            return null;
        }

        if (getSpaceAt(nextRow, nextCol) == null) {
            return null;
        }

        Space next = getSpaceAt(nextRow, nextCol);
        if (next == null) {
            return null;
        }

        if (matchedSpace.getPlayerId().equals(next.getWhichPlayer())) {
            return checkNext(new MatchedSpace(matchedSpace.getPlayerId(), nextRow, nextCol, matchedSpace.getNumInARow() + 1, matchedSpace.getDir()));
        }

        return null;
    }
}

class MatchedSpace {
    private String playerId;
    private int rowIndex;
    private int colIndex;

    private int numInARow;
    private DIR dir;

    public MatchedSpace(String playerId, int rowIndex, int colIndex, int numInARow, DIR dir) {
        this.playerId = playerId;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.numInARow = numInARow;
        this.dir = dir;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getNumInARow() {
        return numInARow;
    }

    public DIR getDir() {
        return dir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchedSpace matchedSpace = (MatchedSpace) o;
        return getRowIndex() == matchedSpace.getRowIndex() &&
                getColIndex() == matchedSpace.getColIndex() &&
                Objects.equals(getPlayerId(), matchedSpace.getPlayerId()) &&
                getDir() == matchedSpace.getDir();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerId(), getRowIndex(), getColIndex(), getDir());
    }
}

enum DIR {
    RIGHT, UP, DIAGONAL, BACK_DIAGONAL
}
