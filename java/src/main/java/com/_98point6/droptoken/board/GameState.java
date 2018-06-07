package com._98point6.droptoken.board;

import com._98point6.droptoken.ConflictException;
import com._98point6.droptoken.Constants;
import com._98point6.droptoken.GoneException;
import com._98point6.droptoken.model.CreateGameRequest;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private final List<String> initialPlayers;
    private List<String> activePlayers;
    private List<Move> moveHistory;
    private int turn;
    private String winner;

    private Column[] spaces;

    public GameState(CreateGameRequest board) {
        moveHistory = new ArrayList<>();
        initialPlayers = board.getPlayers();
        activePlayers = initialPlayers;
        turn = 0;

        spaces = new Column[board.getColumns()];
        for (int i = 0; i < spaces.length; i++) {
            spaces[i] = new Column(board.getRows());
        }
    }

    public Column[] getSpaces() {
        return spaces;
    }

    public Move getMove(int moveNumber) {
        try {
            return moveHistory.get(moveNumber);
        } catch (IndexOutOfBoundsException e) {
            throw new NotFoundException(e);
        }
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public List<Move> getMoveHistory(int start, int end) {
        try {
            return moveHistory.subList(start, end + 1);
        } catch (IndexOutOfBoundsException e) {
            throw new NotFoundException(e);
        }
    }

    public int dropToken(String player, int columnNum) {
        if (!isPlayerInGame(player)) {
            throw new NotFoundException();
        }

        if (!isPlayersTurn(player)) {
            throw new ConflictException();
        }

        if (winner == null) {
            if (spaces[columnNum].dropToken(player)) {
                turn++;
                if (turn == activePlayers.size()) {
                    turn = 0;
                }

                int moveId = saveMove(Constants.MOVE_TYPE.MOVE, player, columnNum);

                checkAndProcessEndGame();

                return moveId;
            }
        }

        throw new BadRequestException();
    }

    public void quit(String player) {
        if (winner != null) {
            throw new GoneException();
        }

        if (!isPlayerInGame(player)) {
            throw new NotFoundException();
        }

        String playerTurn = activePlayers.get(turn);
        activePlayers.remove(player);
        if(player.equals(playerTurn))
        {
            if (turn == activePlayers.size()) {
                turn = 0;
            }
        } else {
            turn = activePlayers.indexOf(playerTurn);
        }

        saveMove(Constants.MOVE_TYPE.QUIT, player, null);

        checkAndProcessEndGame();
    }

    private int saveMove(Constants.MOVE_TYPE type, String player, Integer columnNum) {
        Move move = new Move(type.toString(), player, columnNum);

        int moveId = moveHistory.size();
        moveHistory.add(move);
        return moveId;
    }

    private boolean isPlayerInGame(String player) {
        return activePlayers.contains(player);
    }

    private boolean isPlayersTurn(String player) {
        return player.equals(activePlayers.get(turn));
    }

    private boolean hasEnoughPlayers() {
        return activePlayers.size() > 1;
    }

    /**
     * @return winner's playerId
     */
    private String whoIsTheWinner() {
        if (!hasEnoughPlayers()) {
            return activePlayers.get(0);
        }

        WinEvaluator winEvaluator = new WinEvaluator(this);
        return winEvaluator.whoWon();
    }

    private void checkAndProcessEndGame() {
        String winner = whoIsTheWinner();
        if (winner != null) {
            this.winner = winner;
        }
    }

    public List<String> getInitialPlayers() {
        return initialPlayers;
    }

    public String getWinner() {
        return winner;
    }
}