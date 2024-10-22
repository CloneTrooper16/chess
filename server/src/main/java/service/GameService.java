package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import handler.Handler;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDataAccess;
    private final AuthDAO authDataAccess;


    public GameService(GameDAO gameDataAccess, AuthDAO authDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (isValidAuth(authToken)) {
            var game = gameDataAccess.createGame(gameName);
            return game.gameID();
        }
        throw new DataAccessException("unauthorized");
    }

    public void joinGame(String authToken, Handler.JoinGameRequest joinGameRequest) throws DataAccessException {
        if (isValidAuth(authToken)) {
            if (isValidGameID(joinGameRequest.gameID())) {
                GameData gameData = getGame(joinGameRequest.gameID());
                var authData = authDataAccess.getAuth(authToken);
                if (isValidColor(joinGameRequest.playerColor())) {
                    if (isColorAvailable(gameData, joinGameRequest.playerColor())) {
                        var newGame = updateGamePlayers(gameData, authData.username(), joinGameRequest.playerColor());
                        gameDataAccess.updateGame(gameData.gameID(), newGame);
                        return;
                    }
                    throw new DataAccessException("already taken");
                }
            }
            throw new DataAccessException("bad request");
        }
        throw new DataAccessException("unauthorized");
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException{
        if (isValidAuth(authToken)) {
            return gameDataAccess.listGames();
        }
        throw new DataAccessException("unauthorized");
    }

    private boolean isValidAuth(String authToken) throws DataAccessException {
        return authDataAccess.getAuth(authToken) != null;
    }

    private GameData getGame(int gameID) throws DataAccessException {
        return gameDataAccess.getGame(gameID);
    }

    private boolean isValidGameID(int gameID) throws DataAccessException {
        return gameDataAccess.getGame(gameID) != null;
    }

    private boolean isValidColor(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE || color == ChessGame.TeamColor.BLACK;
    }

    private boolean isColorAvailable(GameData gameData, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return gameData.whiteUsername() == null;
        }
        return gameData.blackUsername() == null;
    }

    private GameData updateGamePlayers(GameData gameData, String username, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return new GameData(gameData.gameID(), username, gameData.blackUsername(), gameData.gameName(), gameData.game());
        }
        return new GameData(gameData.gameID(), gameData.whiteUsername(), username, gameData.gameName(), gameData.game());
    }

    public void deleteAllGames() throws DataAccessException {
        gameDataAccess.clear();
    }
}
