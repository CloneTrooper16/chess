package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.ServerException;
import dataaccess.GameDAO;
import handler.Handler;
import model.GameData;
import model.JoinGameRequest;

import java.util.Collection;

public class GameService {
    private final GameDAO gameDataAccess;
    private final AuthDAO authDataAccess;


    public GameService(GameDAO gameDataAccess, AuthDAO authDataAccess) {
        this.gameDataAccess = gameDataAccess;
        this.authDataAccess = authDataAccess;
    }

    public int createGame(String authToken, String gameName) throws ServerException {
        if (isValidAuth(authToken)) {
            var game = gameDataAccess.createGame(gameName);
            return game.gameID();
        }
        throw new ServerException("unauthorized");
    }

    public void joinGame(String authToken, JoinGameRequest joinGameRequest) throws ServerException {
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
                    throw new ServerException("already taken");
                }
            }
            throw new ServerException("bad request");
        }
        throw new ServerException("unauthorized");
    }

    public Collection<GameData> listGames(String authToken) throws ServerException {
        if (isValidAuth(authToken)) {
            return gameDataAccess.listGames();
        }
        throw new ServerException("unauthorized");
    }

    private boolean isValidAuth(String authToken) throws ServerException {
        return authDataAccess.getAuth(authToken) != null;
    }

    public GameData getGame(int gameID) throws ServerException {
        return gameDataAccess.getGame(gameID);
    }

    private boolean isValidGameID(int gameID) throws ServerException {
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

    public void deleteAllGames() throws ServerException {
        gameDataAccess.clear();
    }

    public int getNextID() {
        return gameDataAccess.getNextID();
    }
}
