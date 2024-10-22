package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO{
    private int nextID = 1;
    private final HashMap<Integer, GameData> games = new HashMap<>();

    public GameData createGame(String gameName) throws DataAccessException {
        var game = new GameData(nextID++, null, null, gameName, new ChessGame());
        games.put(game.gameID(), game);
        return game;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
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
