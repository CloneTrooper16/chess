package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    GameData createGame(String gameName) throws ServerException;
    GameData getGame(int gameID) throws ServerException;
    Collection<GameData> listGames() throws ServerException;
    void updateGame(int gameID, GameData gameData) throws ServerException;

    void clear() throws ServerException;

    int getNextID();
}
