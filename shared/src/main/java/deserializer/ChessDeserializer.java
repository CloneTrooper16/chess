package deserializer;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.rulebook.FIDERuleBook;
import chess.rulebook.RuleBook;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ChessDeserializer {

   public Gson createChessGson() {
       return new GsonBuilder()
           .registerTypeAdapter(ChessPosition.class, new ChessPositionDeserializer())
           .registerTypeAdapter(ChessPiece.class, new ChessPieceDeserializer())
           .registerTypeAdapter(new TypeToken<Map<ChessPosition, ChessPiece>>() {
           }.getType(), new CustomPieceMapDeserializer())
           .registerTypeAdapter(new TypeToken<Map<ChessPiece, ChessPosition>>() {
           }.getType(), new CustomPositionMapDeserializer())
           .registerTypeAdapter(RuleBook.class, new RuleBookInstanceCreator())
           .create();
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
