package dataaccess;

import model.GameData;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.HashSet;

public class DatabaseGameDAO implements GameDAO {
    public GameData createGame(String gameName) throws DataAccessException {
        return new GameData(1, null, null, "test", null);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return new GameData(1, null, null, "test", null);
    }

    public Collection<GameData> listGames() throws DataAccessException {
        return new HashSet<GameData>();
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {

    }

    public void clear() throws DataAccessException {

    }
}
