package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DatabaseGameDAO implements GameDAO {
    public DatabaseGameDAO() throws ServerException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public GameData createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (white_username, black_username, game_name, json) VALUES (?, ?, ?, ?)";
        ChessGame newChessGame = new ChessGame();
        var json = new Gson().toJson(newChessGame);
        var id = executeUpdate(statement, null, null, gameName, json);
        return new GameData(id, null, null, gameName, newChessGame);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, white_username, black_username, game_name, json FROM games WHERE gameID=?";
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
        return new HashSet<GameData>();
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {

    }

    public void clear() throws DataAccessException {

    }

    public int getNextID() {
        return 1;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var white_username = rs.getString("white_username");
        var black_username = rs.getString("black_username");
        var game_name = rs.getString("game_name");
        var jsonString = rs.getString("json");
        ChessGame game = new Gson().fromJson(jsonString, ChessGame.class);
        return new GameData(gameID, white_username, black_username, game_name, game);
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS games (
          `id` int NOT NULL AUTO_INCREMENT,
          `white_username` varchar(256),
          `black_username` varchar(256),
          `game_name` varchar(256) NOT NULL,
          `json` TEXT NOT NULL,
          PRIMARY KEY (`id`),
          INDEX(game_name)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var chess = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> chess.setString(i + 1, p);
                        case Integer p -> chess.setInt(i + 1, p);
                        case null -> chess.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                chess.executeUpdate();

                var rs = chess.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
