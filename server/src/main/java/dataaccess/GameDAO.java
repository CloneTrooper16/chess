package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Collection<GameData> listGames(String authToken) throws DataAccessException;
    void updateGame(int gameID, GameData gameData) throws DataAccessException;

    void clear() throws DataAccessException;
}
