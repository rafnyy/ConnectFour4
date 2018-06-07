package com._98point6.droptoken.database;

import com._98point6.droptoken.board.GameState;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class GamesTable {
    private DatabaseConnection databaseConnection;

    private String TABLE_NAME = "Games";

    private Logger logger;

    private String ID = "id";
    private String STATE = "game_state";

    @Inject
    public GamesTable(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        if (!doesTableExist()) {
            List<String> clauses = new ArrayList<>();
            StringBuilder createSql = new StringBuilder("CREATE TABLE " + TABLE_NAME + " (");

            clauses.add(ID + " VARCHAR(255) NOT NULL PRIMARY KEY");
            clauses.add(STATE + " VARCHAR(5000) NOT NULL");

            createSql.append(Joiner.on(',').join(clauses));
            createSql.append(")");

            NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), createSql.toString());
            preparedStatement.execute();
        }
    }

    private boolean doesTableExist() throws SQLException {
        ResultSet tables = databaseConnection.getConnection().getMetaData().getTables("", null, null, null);
        while (tables.next()) {
            String nextTable = tables.getString("TABLE_NAME");
            if (StringUtils.equalsIgnoreCase(TABLE_NAME, nextTable)) {
                return true;
            }
        }

        return false;
    }

    public GameState getGameState(String id) throws SQLException {
        Gson gson = new Gson();

        NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), "SELECT " + STATE + " FROM " + TABLE_NAME + " WHERE " + ID + " =:id");
        preparedStatement.setString("id", id);
        ResultSet results = preparedStatement.executeQuery();

        if (results.next()) {
            String json = results.getString(STATE);
            return gson.fromJson(json, GameState.class);
        }

        throw new NotFoundException();
    }

    public List<String> getAllGameIds() throws SQLException {
        List<String> states = new ArrayList<>();

        NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), "SELECT " + ID + " FROM " + TABLE_NAME);
        ResultSet results = preparedStatement.executeQuery();

        while (results.next()) {
            states.add(results.getString(ID));
        }

        return states;
    }

    public Set<GameState> getAllGameStates() throws SQLException {
        Set<GameState> states = new HashSet<>();
        Gson gson = new Gson();

        NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), "SELECT " + STATE + " FROM " + TABLE_NAME);
        ResultSet results = preparedStatement.executeQuery();

        while (results.next()) {
            String json = results.getString(STATE);
            states.add(gson.fromJson(json, GameState.class));
        }

        return states;
    }

    public String insertNewGame(GameState gameState) throws SQLException {
        Gson gson = new Gson();

        String id = UUID.randomUUID().toString();

        NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), "INSERT into " + TABLE_NAME + " (" + ID + ", " + STATE + ") values (:id, :state)");
        preparedStatement.setString("id", id);
        preparedStatement.setString("state", gson.toJson(gameState));
        preparedStatement.execute();

        return id;
    }

    public void updateGameState(String id, GameState gameState) throws SQLException {
        Gson gson = new Gson();

        NamedParameterStatement preparedStatement = new NamedParameterStatement(databaseConnection.getConnection(), "UPDATE " + TABLE_NAME + " SET " + STATE + " =:state WHERE " + ID + " =:id");
        preparedStatement.setString("id", id);
        preparedStatement.setString("state", gson.toJson(gameState));
        preparedStatement.execute();

    }


}
