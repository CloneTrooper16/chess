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

    public Collection<GameData> listGames() throws DataAccessException {
        HashSet<GameData> allGames = new HashSet<>();
        for (Integer id : games.keySet()) {
            allGames.add(games.get(id));
        }
        return allGames;
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        games.put(gameID, gameData);
    }

    public void clear() throws DataAccessException {
        games.clear();
    }

    public int getNextID() {
        return nextID;
    }
}
