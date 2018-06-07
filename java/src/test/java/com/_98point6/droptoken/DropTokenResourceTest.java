package com._98point6.droptoken;

import com._98point6.droptoken.database.DatabaseConnection;
import com._98point6.droptoken.database.GamesTable;
import com._98point6.droptoken.model.*;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.MockitoAnnotations.initMocks;

public class DropTokenResourceTest {
    private GamesTable gamesTable;

    private DropTokenResource dropTokenResource;

    private List<String> players;
    private String player1 = "player1";
    private String player2 = "player2";

    private Integer columns;
    private Integer rows;

    private Connection conn;

    @Mock
    private DatabaseConnection databaseConnection;

    @Before
    public void setUp() throws Exception {

        initMocks(this);

        Driver derbyEmbeddedDriver = new EmbeddedDriver();
        DriverManager.registerDriver(derbyEmbeddedDriver);
        Properties props = new Properties();

        String dbName = "testDB"; // the name of the database
        conn = DriverManager.getConnection("jdbc:derby:" + dbName
                + ";create=true", props);
        conn.setAutoCommit(false);

        Mockito.when(databaseConnection.getConnection()).thenReturn(conn);

        gamesTable = new GamesTable(databaseConnection);
        dropTokenResource = new DropTokenResource(gamesTable);
        players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        columns = 4;
        rows = 4;
    }

    @After
    public void tearDown() throws Exception {
        try {
            DriverManager.getConnection
                    ("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getGames() throws SQLException {
        String gameId1 = createBasicGame();
        String gameId2 = createBasicGame();

        Response response = dropTokenResource.getGames();
        assertThat(response.getStatus(), is(equalTo(HttpStatus.OK_200)));
        assertThat(response.getEntity(), is(instanceOf(GetGamesResponse.class)));

        GetGamesResponse getGamesResponse = (GetGamesResponse) response.getEntity();
        assertThat(getGamesResponse.getGames().size(), is(equalTo(2)));
        assertThat(getGamesResponse.getGames(), containsInAnyOrder(gameId1, gameId2));
    }

    @Test
    public void createNewGame() throws SQLException {
        createBasicGame();
    }

    @Test(expected = BadRequestException.class)
    public void createNewGameRowTooSmall() throws SQLException {
        CreateGameRequest.Builder builder = new CreateGameRequest.Builder();
        builder.players(players).columns(columns).rows(Constants.winSize - 1);
        CreateGameRequest createGameRequest = builder.build();
        dropTokenResource.createNewGame(createGameRequest);
    }

    @Test(expected = BadRequestException.class)
    public void createNewGameColumnTooSmall() throws SQLException {
        CreateGameRequest.Builder builder = new CreateGameRequest.Builder();
        builder.players(players).columns(Constants.winSize - 1).rows(rows);
        CreateGameRequest createGameRequest = builder.build();
        dropTokenResource.createNewGame(createGameRequest);
    }

    private String createBasicGame() throws SQLException {
        CreateGameRequest.Builder builder = new CreateGameRequest.Builder();
        builder.players(players).columns(columns).rows(rows);
        CreateGameRequest createGameRequest = builder.build();
        Response response = dropTokenResource.createNewGame(createGameRequest);
        assertThat(response.getStatus(), is(equalTo(HttpStatus.OK_200)));
        assertThat(response.getEntity(), is(instanceOf(CreateGameResponse.class)));

        CreateGameResponse createGameResponse = (CreateGameResponse) response.getEntity();
        String gameId = createGameResponse.getGameId();
        assertThat(gameId, is(notNullValue()));
        return gameId;
    }

    @Test
    public void getGameStatus() throws SQLException {
        String gameId = createBasicGame();

        Response response = dropTokenResource.getGameStatus(gameId);
        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse = (GameStatusResponse) response.getEntity();
        assertThat(gameStatusResponse.getMoves(), is(equalTo(0)));
        assertThat(gameStatusResponse.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse.getPlayers(), is(equalTo(players)));
    }

    @Test(expected = NotFoundException.class)
    public void getGameStatusNotFound() throws SQLException {
        createBasicGame();

        dropTokenResource.getGameStatus("fake game id");
    }

    @Test
    public void postMove() throws SQLException {
        String gameId = createBasicGame();

        String moveLink = postBasicMove(gameId, 0, player1);
        assertThat(moveLink, is(equalTo(gameId + "/moves/" + 0)));
    }

    @Test(expected = BadRequestException.class)
    public void postMoveIllegalColumnFull() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 0, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 0, player2);
        postBasicMove(gameId, 0, player1);
    }

    @Test(expected = BadRequestException.class)
    public void postMoveIllegalGameOver() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 3, player2);
        postBasicMove(gameId, 1, player1);
    }

    @Test(expected = NotFoundException.class)
    public void postMoveNoGame() throws SQLException {
        createBasicGame();

        postBasicMove("fake game id", 0, player1);
    }

    @Test(expected = NotFoundException.class)
    public void postMovePlayerNotInGame() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, "player3");
    }

    @Test(expected = ConflictException.class)
    public void postMoveNotTurn() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player2);
    }

    @Test
    public void postMoveWinByColumn() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 2, player2);

        Response response1 = dropTokenResource.getGameStatus(gameId);
        assertThat(response1.getStatus(), is(HttpStatus.OK_200));
        assertThat(response1.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse1 = (GameStatusResponse) response1.getEntity();
        assertThat(gameStatusResponse1.getMoves(), is(equalTo(6)));
        assertThat(gameStatusResponse1.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse1.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));

        // winning move
        postBasicMove(gameId, 1, player1);

        Response response2 = dropTokenResource.getGameStatus(gameId);
        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
        assertThat(response2.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse2 = (GameStatusResponse) response2.getEntity();
        assertThat(gameStatusResponse2.getMoves(), is(equalTo(7)));
        assertThat(gameStatusResponse2.getWinner().get(), is(equalTo(player1)));
        assertThat(gameStatusResponse2.getState(), is(equalTo(Constants.STATE.DONE.toString())));
        assertThat(gameStatusResponse2.getPlayers(), is(equalTo(players)));
    }

    @Test
    public void postMoveWinByRow() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 0, player2);
        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 2, player1);
        postBasicMove(gameId, 2, player2);

        Response response1 = dropTokenResource.getGameStatus(gameId);
        assertThat(response1.getStatus(), is(HttpStatus.OK_200));
        assertThat(response1.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse1 = (GameStatusResponse) response1.getEntity();
        assertThat(gameStatusResponse1.getMoves(), is(equalTo(6)));
        assertThat(gameStatusResponse1.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse1.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));

        // winning move
        postBasicMove(gameId, 3, player1);

        Response response2 = dropTokenResource.getGameStatus(gameId);
        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
        assertThat(response2.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse2 = (GameStatusResponse) response2.getEntity();
        assertThat(gameStatusResponse2.getMoves(), is(equalTo(7)));
        assertThat(gameStatusResponse2.getWinner().get(), is(equalTo(player1)));
        assertThat(gameStatusResponse2.getState(), is(equalTo(Constants.STATE.DONE.toString())));
        assertThat(gameStatusResponse2.getPlayers(), is(equalTo(players)));
    }

    @Test
    public void postMoveWinByDiagonal() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 3, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 2, player1);
        postBasicMove(gameId, 3, player2);
        postBasicMove(gameId, 3, player1);
        postBasicMove(gameId, 0, player2);

        Response response1 = dropTokenResource.getGameStatus(gameId);
        assertThat(response1.getStatus(), is(HttpStatus.OK_200));
        assertThat(response1.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse1 = (GameStatusResponse) response1.getEntity();
        assertThat(gameStatusResponse1.getMoves(), is(equalTo(10)));
        assertThat(gameStatusResponse1.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse1.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));

        // winning move
        postBasicMove(gameId, 3, player1);

        Response response2 = dropTokenResource.getGameStatus(gameId);
        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
        assertThat(response2.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse2 = (GameStatusResponse) response2.getEntity();
        assertThat(gameStatusResponse2.getMoves(), is(equalTo(11)));
        assertThat(gameStatusResponse2.getWinner().get(), is(equalTo(player1)));
        assertThat(gameStatusResponse2.getState(), is(equalTo(Constants.STATE.DONE.toString())));
        assertThat(gameStatusResponse2.getPlayers(), is(equalTo(players)));
    }

    @Test
    public void postMoveWinByBackwardsDiagonal() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 3, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 2, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 1, player1);
        postBasicMove(gameId, 0, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 3, player2);

        Response response1 = dropTokenResource.getGameStatus(gameId);
        assertThat(response1.getStatus(), is(HttpStatus.OK_200));
        assertThat(response1.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse1 = (GameStatusResponse) response1.getEntity();
        assertThat(gameStatusResponse1.getMoves(), is(equalTo(10)));
        assertThat(gameStatusResponse1.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse1.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));

        // winning move
        postBasicMove(gameId, 0, player1);

        Response response2 = dropTokenResource.getGameStatus(gameId);
        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
        assertThat(response2.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse2 = (GameStatusResponse) response2.getEntity();
        assertThat(gameStatusResponse2.getMoves(), is(equalTo(11)));
        assertThat(gameStatusResponse2.getWinner().get(), is(equalTo(player1)));
        assertThat(gameStatusResponse2.getState(), is(equalTo(Constants.STATE.DONE.toString())));
        assertThat(gameStatusResponse2.getPlayers(), is(equalTo(players)));
    }

    private String postBasicMove(String gameId, int column, String player) throws SQLException {
        PostMoveRequest.Builder builder = new PostMoveRequest.Builder();
        PostMoveRequest postMoveRequest = builder.column(column).build();

        Response response = dropTokenResource.postMove(gameId, player, postMoveRequest);
        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getEntity(), instanceOf(PostMoveResponse.class));

        PostMoveResponse postMoveResponse = (PostMoveResponse) response.getEntity();
        String moveLink = postMoveResponse.getMoveLink();

        return moveLink;
    }

    @Test
    public void playerQuit() throws SQLException {
        String gameId = createBasicGame();

        Response response = dropTokenResource.playerQuit(gameId, player1);
        assertThat(response.getStatus(), is(HttpStatus.ACCEPTED_202));
    }

    @Test
    public void playerQuitGameContinues() throws SQLException {
        String player3 = "player3";
        players.add(player3);
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);

        Response response = dropTokenResource.playerQuit(gameId, player1);
        assertThat(response.getStatus(), is(HttpStatus.ACCEPTED_202));

        postBasicMove(gameId, 0, player2);
        postBasicMove(gameId, 0, player3);
        postBasicMove(gameId, 0, player2);
    }

    @Test
    public void playerQuitCausesWin() throws SQLException {
        String gameId = createBasicGame();

        Response response1 = dropTokenResource.getGameStatus(gameId);
        assertThat(response1.getStatus(), is(HttpStatus.OK_200));
        assertThat(response1.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse1 = (GameStatusResponse) response1.getEntity();
        assertThat(gameStatusResponse1.getMoves(), is(equalTo(0)));
        assertThat(gameStatusResponse1.getWinner().isPresent(), is(equalTo(false)));
        assertThat(gameStatusResponse1.getState(), is(equalTo(Constants.STATE.IN_PROGRESS.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));

        // winning move
        Response response = dropTokenResource.playerQuit(gameId, player1);
        assertThat(response.getStatus(), is(HttpStatus.ACCEPTED_202));

        Response response2 = dropTokenResource.getGameStatus(gameId);
        assertThat(response2.getStatus(), is(HttpStatus.OK_200));
        assertThat(response2.getEntity(), instanceOf(GameStatusResponse.class));

        GameStatusResponse gameStatusResponse2 = (GameStatusResponse) response2.getEntity();
        assertThat(gameStatusResponse2.getMoves(), is(equalTo(1)));
        assertThat(gameStatusResponse2.getWinner().get(), is(equalTo(player2)));
        assertThat(gameStatusResponse2.getState(), is(equalTo(Constants.STATE.DONE.toString())));
        assertThat(gameStatusResponse1.getPlayers(), is(equalTo(players)));
    }

    @Test(expected = NotFoundException.class)
    public void playerQuitNoGame() throws SQLException {
        createBasicGame();

        dropTokenResource.playerQuit("fake game id", player1);
    }

    @Test(expected = NotFoundException.class)
    public void playerQuitNotInGame() throws SQLException {
        String gameId = createBasicGame();

        dropTokenResource.playerQuit(gameId, "player3");
    }

    @Test(expected = GoneException.class)
    public void playerQuitIllegalGameOver() throws SQLException {
        String gameId = createBasicGame();

        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 2, player2);
        postBasicMove(gameId, 0, player1);
        postBasicMove(gameId, 1, player2);
        postBasicMove(gameId, 0, player1);
        dropTokenResource.playerQuit(gameId, player1);
    }

    @Test
    public void getAllMoves() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);
        dropTokenResource.playerQuit(gameId, player2);

        Response response = dropTokenResource.getMoves(gameId, 0, 4);
        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getEntity(), instanceOf(GetMovesResponse.class));

        GetMovesResponse getMovesResponse = (GetMovesResponse) response.getEntity();
        assertThat(getMovesResponse.getMoves().size(), is(equalTo(5)));

        GetMoveResponse getMoveResponse1 = getMovesResponse.getMoves().get(0);
        assertThat(getMoveResponse1.getColumn().get(), is(equalTo(0)));
        assertThat(getMoveResponse1.getPlayer(), is(equalTo(player1)));
        assertThat(getMoveResponse1.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));

        GetMoveResponse getMoveResponse2 = getMovesResponse.getMoves().get(1);
        assertThat(getMoveResponse2.getColumn().get(), is(equalTo(1)));
        assertThat(getMoveResponse2.getPlayer(), is(equalTo(player2)));
        assertThat(getMoveResponse2.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));

        GetMoveResponse getMoveResponse3 = getMovesResponse.getMoves().get(2);
        assertThat(getMoveResponse3.getColumn().get(), is(equalTo(2)));
        assertThat(getMoveResponse3.getPlayer(), is(equalTo(player1)));
        assertThat(getMoveResponse3.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));

        GetMoveResponse getMoveResponse4 = getMovesResponse.getMoves().get(3);
        assertThat(getMoveResponse4.getColumn().get(), is(equalTo(3)));
        assertThat(getMoveResponse4.getPlayer(), is(equalTo(player2)));
        assertThat(getMoveResponse4.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));

        GetMoveResponse getMoveResponse5 = getMovesResponse.getMoves().get(4);
        assertThat(getMoveResponse5.getColumn().isPresent(), is(equalTo(false)));
        assertThat(getMoveResponse5.getPlayer(), is(equalTo(player2)));
        assertThat(getMoveResponse5.getType(), is(equalTo(Constants.MOVE_TYPE.QUIT.toString())));
    }

    @Test
    public void getRangeMoves() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);

        Response response = dropTokenResource.getMoves(gameId, 1, 2);
        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getEntity(), instanceOf(GetMovesResponse.class));


        GetMovesResponse getMovesResponse = (GetMovesResponse) response.getEntity();
        assertThat(getMovesResponse.getMoves().size(), is(equalTo(2)));

        GetMoveResponse getMoveResponse2 = getMovesResponse.getMoves().get(0);
        assertThat(getMoveResponse2.getColumn().get(), is(equalTo(1)));
        assertThat(getMoveResponse2.getPlayer(), is(equalTo(player2)));
        assertThat(getMoveResponse2.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));

        GetMoveResponse getMoveResponse3 = getMovesResponse.getMoves().get(1);
        assertThat(getMoveResponse3.getColumn().get(), is(equalTo(2)));
        assertThat(getMoveResponse3.getPlayer(), is(equalTo(player1)));
        assertThat(getMoveResponse3.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));
    }

    @Test(expected = NotFoundException.class)
    public void getMovesGameNotFound() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);

        dropTokenResource.getMoves("fake game id", 0, 1);
    }

    @Test(expected = NotFoundException.class)
    public void getMovesNotFound() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);

        dropTokenResource.getMoves(gameId, 5, 5);
    }

    @Test
    public void getMove() throws SQLException {
        String gameId = createBasicGame();

        String moveLink1 = postBasicMove(gameId, 0, player1);
        assertThat(moveLink1, is(equalTo(gameId + "/moves/" + 0)));
        String moveLink2 = postBasicMove(gameId, 1, player2);
        assertThat(moveLink2, is(equalTo(gameId + "/moves/" + 1)));

        Response response = dropTokenResource.getMove(gameId, 1);
        assertThat(response.getStatus(), is(HttpStatus.OK_200));
        assertThat(response.getEntity(), instanceOf(GetMoveResponse.class));

        GetMoveResponse getMoveResponse = (GetMoveResponse) response.getEntity();
        assertThat(getMoveResponse.getColumn().get(), is(equalTo(1)));
        assertThat(getMoveResponse.getPlayer(), is(equalTo(player2)));
        assertThat(getMoveResponse.getType(), is(equalTo(Constants.MOVE_TYPE.MOVE.toString())));
    }

    @Test(expected = NotFoundException.class)
    public void getMoveGameNotFound() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);

        dropTokenResource.getMove("fake game id", 0);
    }

    @Test(expected = NotFoundException.class)
    public void getMoveNotFound() throws SQLException {
        String gameId = createBasicGame();

        postFourBasicMoves(gameId);

        dropTokenResource.getMove(gameId, 5);
    }

    private void postFourBasicMoves(String gameId) throws SQLException {
        String moveLink1 = postBasicMove(gameId, 0, player1);
        assertThat(moveLink1, is(equalTo(gameId + "/moves/" + 0)));
        String moveLink2 = postBasicMove(gameId, 1, player2);
        assertThat(moveLink2, is(equalTo(gameId + "/moves/" + 1)));
        String moveLink3 = postBasicMove(gameId, 2, player1);
        assertThat(moveLink3, is(equalTo(gameId + "/moves/" + 2)));
        String moveLink4 = postBasicMove(gameId, 3, player2);
        assertThat(moveLink4, is(equalTo(gameId + "/moves/" + 3)));
    }
}