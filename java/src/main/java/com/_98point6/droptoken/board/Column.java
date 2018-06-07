package com._98point6.droptoken.board;

public class Column {
    private Space[] spaces;
    private boolean isFull;
    private int firstEmpty;


    public Column(int height) {
        spaces = new Space[height];
        isFull = false;
        firstEmpty = 0;
    }

    public Space[] getSpaces() {
        return spaces;
    }

    public boolean isFull() {
        return isFull;
    }

    /**
     * @param player player id
     * @return whether the token can be successfully dropped or not
     */
    public boolean dropToken(String player) {
        if (isFull) {
            return false;
        }

        spaces[firstEmpty] = new Space(player);
        firstEmpty++;
        if (firstEmpty >= spaces.length) {
            isFull = true;
        }

        return true;
    }
}
