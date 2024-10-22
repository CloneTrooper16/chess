package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import handler.Handler;
import model.AuthData;
import model.GameData;

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

            }
            throw new DataAccessException("bad request");
        }
        throw new DataAccessException("unauthorized");
    }

    private boolean isValidAuth(String authToken) throws DataAccessException {
        return authDataAccess.getAuth(authToken) != null;
    }

    private boolean isValidGameID(int gameID) throws DataAccessException {
        return gameDataAccess.getGame(gameID) != null;
    }
}
