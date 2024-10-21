package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String authToken) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames(String authToken) throws DataAccessException;
    GameData updateGame(String authToken, int gameID) throws DataAccessException;

    void clear() throws DataAccessException;
}
