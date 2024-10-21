package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashSet;

public class MemoryGameDAO {
    public GameData createGame(String authToken) throws DataAccessException {
        return new GameData(1, "w", "b", "test", new ChessGame());
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return new GameData(1, "w", "b", "test", new ChessGame());
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return new HashSet<GameData>();
    }

    public GameData updateGame(String authToken, int gameID) throws DataAccessException {
        return new GameData(1, "w", "b", "test", new ChessGame());
    }

    public void clear() throws DataAccessException {

    }
}
