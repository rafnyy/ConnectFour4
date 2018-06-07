package com._98point6.droptoken;

import com._98point6.droptoken.board.GameState;
import com._98point6.droptoken.board.Move;
import com._98point6.droptoken.database.GamesTable;
import com._98point6.droptoken.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Path("/drop_token")
@Produces(MediaType.APPLICATION_JSON)
public class DropTokenResource {
    private static final Logger logger = LoggerFactory.getLogger(DropTokenResource.class);

    private GamesTable gamesTable;

    @Inject
    public DropTokenResource(GamesTable gamesTable) {
        this.gamesTable = gamesTable;
    }

    @GET
    public Response getGames() throws SQLException {
        List<String> ids = gamesTable.getAllGameIds();

        GetGamesResponse.Builder builder = new GetGamesResponse.Builder();
        builder.games(ids);
        GetGamesResponse getGamesResponse = builder.build();

        return Response.ok(getGamesResponse).build();
    }

    @POST
    public Response createNewGame(CreateGameRequest request) throws SQLException {
        logger.info("request={}", request);

        if (request.getColumns() < Constants.winSize || request.getRows() < Constants.winSize) {
            throw new BadRequestException("Size too small, rows and columns must be equal to or greater than " + Constants.winSize);
        }

        GameState gameState = new GameState(request);
        String id = gamesTable.insertNewGame(gameState);

        CreateGameResponse.Builder builder = new CreateGameResponse.Builder();
        CreateGameResponse createGameResponse = builder.gameId(id).build();

        return Response.ok(createGameResponse).build();
    }

    @Path("/{id}")
    @GET
    public Response getGameStatus(@PathParam("id") String gameId) throws SQLException {
        logger.info("gameId = {}", gameId);
        GameState gameState = gamesTable.getGameState(gameId);

        GameStatusResponse.Builder builder = new GameStatusResponse.Builder();
        builder.players(gameState.getInitialPlayers()).moves(gameState.getMoveHistory().size());

        if (gameState.getWinner() == null) {
            builder.state(Constants.STATE.IN_PROGRESS.toString());

        } else {
            builder.winner(gameState.getWinner());
            builder.state(Constants.STATE.DONE.toString());
        }

        GameStatusResponse gameStatusResponse = builder.build();

        return Response.ok(gameStatusResponse).build();
    }

    @Path("/{id}/{playerId}")
    @POST
    public Response postMove(@PathParam("id") String gameId, @PathParam("playerId") String playerId, PostMoveRequest request) throws SQLException {
        logger.info("gameId={}, playerId={}, move={}", gameId, playerId, request);
        //lock row until...
        GameState gameState = gamesTable.getGameState(gameId);
        int moveId = gameState.dropToken(playerId, request.getColumn());

        PostMoveResponse.Builder builder = new PostMoveResponse.Builder();
        PostMoveResponse postMoveResponse = builder.moveLink(gameId + "/moves/" + moveId).build();

        //update games table
        gamesTable.updateGameState(gameId, gameState);
        //... games table is updated

        return Response.ok(postMoveResponse).build();
    }

    @Path("/{id}/{playerId}")
    @DELETE
    public Response playerQuit(@PathParam("id") String gameId, @PathParam("playerId") String playerId) throws SQLException {
        logger.info("gameId={}, playerId={}", gameId, playerId);
        //lock row until...
        GameState gameState = gamesTable.getGameState(gameId);

        gameState.quit(playerId);

        //update games table
        gamesTable.updateGameState(gameId, gameState);
        //... games table is updated

        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Path("/{id}/moves")
    @GET
    public Response getMoves(@PathParam("id") String gameId, @QueryParam("start") Integer start, @QueryParam("until") Integer until) throws SQLException {
        logger.info("gameId={}, start={}, until={}", gameId, start, until);
        GameState gameState = gamesTable.getGameState(gameId);
        List<Move> moves = gameState.getMoveHistory(start, until);
        List<GetMoveResponse> getMovesResponses = new ArrayList<>();
        for (Move move : moves) {
            GetMoveResponse getMoveResponse = getGetMoveResponse(move);
            getMovesResponses.add(getMoveResponse);
        }

        GetMovesResponse.Builder builder = new GetMovesResponse.Builder();
        GetMovesResponse getMovesResponse = builder.moves(getMovesResponses).build();

        return Response.ok(getMovesResponse).build();
    }

    @Path("/{id}/moves/{moveId}")
    @GET
    public Response getMove(@PathParam("id") String gameId, @PathParam("moveId") Integer moveId) throws SQLException {
        logger.info("gameId={}, moveId={}", gameId, moveId);
        GameState gameState = gamesTable.getGameState(gameId);
        Move move = gameState.getMove(moveId);

        GetMoveResponse getMoveResponse = getGetMoveResponse(move);

        return Response.ok(getMoveResponse).build();
    }

    private GetMoveResponse getGetMoveResponse(Move move) {
        GetMoveResponse.Builder builder = new GetMoveResponse.Builder();
        return builder.type(move.getType()).player(move.getPlayer()).column(move.getColumn()).build();
    }
}