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
        var statement = "INSERT INTO games (white_username, black_username, game_name, json) VALUES (?, ?, ?, ?)";
        ChessGame newChessGame = new ChessGame();
        var json = new Gson().toJson(newChessGame);
        var id = executeUpdate(statement, null, null, gameName, json);
        return new GameData(id, null, null, gameName, newChessGame);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, white_username, black_username, game_name, json FROM games WHERE id=?";
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
            var statement = "SELECT id, white_username, black_username, game_name, json FROM games";
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
        var statement = "UPDATE games SET white_username=?, black_username=?, json=?  WHERE id=?";
        var json = new Gson().toJson(gameData.game());
        var id = executeUpdate(statement, gameData.whiteUsername(), gameData.blackUsername(), json, gameID);
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

        Gson gson = new ChessDeserializer().createChessGson();
        ChessGame game = gson.fromJson(jsonString, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
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
        return DatabaseManager.executeUpdate(statement, params);
    }

    private static class ChessPositionDeserializer implements JsonDeserializer<ChessPosition> {
        @Override
        public ChessPosition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                // Parse the JSON string with fromString method
                return ChessPosition.fromString(json.getAsString());
            } else if (json.isJsonObject()) {
                JsonObject jsonObject = json.getAsJsonObject();
                int row = jsonObject.get("row").getAsInt();
                int col = jsonObject.get("col").getAsInt();
                return new ChessPosition(row, col);
            }
            else {
                throw new JsonParseException("Expected a string for ChessPosition, but got: " + json);
            }
        }
    }
    private static class ChessPieceDeserializer implements JsonDeserializer<ChessPiece> {
        @Override
        public ChessPiece deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                // Parse the JSON string with fromString method
                return ChessPiece.fromString(json.getAsString());
            } else if (json.isJsonObject()) {
                // Deserialize from a JSON object format, e.g., {"type":"PAWN","color":"WHITE"}
                JsonObject jsonObject = json.getAsJsonObject();
                ChessPiece.PieceType type = ChessPiece.PieceType.valueOf(jsonObject.get("type").getAsString().toUpperCase());
                ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(jsonObject.get("color").getAsString().toUpperCase());
                return new ChessPiece(color, type);
            } else {
                throw new JsonParseException("Expected a string for ChessPiece, but got: " + json);
            }
        }
    }
    private static class CustomPieceMapDeserializer implements JsonDeserializer<Map<ChessPosition, ChessPiece>> {
        @Override
        public Map<ChessPosition, ChessPiece> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            Map<ChessPosition, ChessPiece> map = new HashMap<>();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                ChessPosition position = ChessPosition.fromString(entry.getKey());
                ChessPiece piece = context.deserialize(entry.getValue(), ChessPiece.class);
                map.put(position, piece);
            }

            return map;
        }
    }

    private static class CustomPositionMapDeserializer implements JsonDeserializer<Map<ChessPiece, ChessPosition>> {
        @Override
        public Map<ChessPiece, ChessPosition> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            Map<ChessPiece, ChessPosition> map = new HashMap<>();
            JsonObject jsonObject = json.getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                ChessPiece piece = ChessPiece.fromString(entry.getKey());
                ChessPosition position = context.deserialize(entry.getValue(), ChessPosition.class);
                map.put(piece, position);
            }

            return map;
        }
    }

    public class RuleBookInstanceCreator implements InstanceCreator<RuleBook> {
        @Override
        public RuleBook createInstance(Type type) {
            return new FIDERuleBook(); // Return an instance of your concrete class
        }
    }
}
