package dataaccess;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.rulebook.FIDERuleBook;
import chess.rulebook.RuleBook;
import com.google.gson.*;
import model.GameData;
import deserializer.ChessDeserializer;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class DatabaseGameDAO implements GameDAO {
    public DatabaseGameDAO() throws ServerException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public GameData createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (white_username, black_username, game_name, json, isOver) VALUES (?, ?, ?, ?, ?)";
        ChessGame newChessGame = new ChessGame();
        var json = new Gson().toJson(newChessGame);
        var id = executeUpdate(statement, null, null, gameName, json, 0);
        return new GameData(id, null, null, gameName, newChessGame, false);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, white_username, black_username, game_name, json, isOver FROM games WHERE id=?";
            try (var chess = conn.prepareStatement(statement)) {
                chess.setInt(1, gameID);
                try (var rs = chess.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, white_username, black_username, game_name, json, isOver FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        var statement = "UPDATE games SET white_username=?, black_username=?, json=?, isOver=?  WHERE id=?";
        var json = new Gson().toJson(gameData.game());
        var isOver = Boolean.toString(gameData.isOver());
        var id = executeUpdate(statement, gameData.whiteUsername(), gameData.blackUsername(),
                json, isOver, gameID);
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

    public int getNextID() {
        return 1;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("id");
        var whiteUsername = rs.getString("white_username");
        var blackUsername = rs.getString("black_username");
        var gameName = rs.getString("game_name");
        var jsonString = rs.getString("json");
        String overString = rs.getString("isOver");
        boolean overBoolean = Boolean.parseBoolean(overString);
        Gson gson = new ChessDeserializer().createChessGson();
        ChessGame game = gson.fromJson(jsonString, ChessGame.class);
        game.setBoard(game.getBoard());

        return new GameData(gameID, whiteUsername, blackUsername, gameName, game, overBoolean);
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS games (
          `id` int NOT NULL AUTO_INCREMENT,
          `white_username` varchar(256),
          `black_username` varchar(256),
          `game_name` varchar(256) NOT NULL,
          `json` TEXT NOT NULL,
          `isOver` char(8),
          PRIMARY KEY (`id`),
          INDEX(game_name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        return DatabaseManager.executeUpdate(statement, params);
    }
}
