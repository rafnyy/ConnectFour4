package com._98point6.droptoken.board;

public class Move {
    private final String type;
    private final String player;
    private final Integer column;

    public Move(String type, String player, Integer column) {
        this.type = type;
        this.player = player;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public String getPlayer() {
        return player;
    }

    public Integer getColumn() {
        return column;
    }
}
