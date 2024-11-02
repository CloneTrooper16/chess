package chess;

import chess.rules.Rules;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final PieceType type;
    private final ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.color = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) obj;
        return type == that.type && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }

    @Override
    public String toString() {
        String result = "";
        switch(type) {
            case KING -> result = "K";
            case QUEEN -> result = "Q";
            case BISHOP -> result = "B";
            case KNIGHT -> result = "N";
            case ROOK -> result = "R";
            case PAWN -> result = "P";
        }
        if (color == ChessGame.TeamColor.BLACK) {
            result = result.toLowerCase();
        }
        return result;
    }

    public static ChessPiece fromString(String piece) {
        if (piece.length() == 1) {
            ChessGame.TeamColor color = ChessGame.TeamColor.WHITE;
            PieceType type = PieceType.KING;
            try {
                if (piece.toLowerCase().equals(piece)) {
                    color = ChessGame.TeamColor.BLACK;
                }
                switch (piece.toLowerCase()) {
                    case "k" -> type = PieceType.KING;
                    case "q" -> type = PieceType.QUEEN;
                    case "b" -> type = PieceType.BISHOP;
                    case "n" -> type = PieceType.KNIGHT;
                    case "r" -> type = PieceType.ROOK;
                    case "p" -> type = PieceType.PAWN;
                }
                return new ChessPiece(color, type);
            } catch (Error e) {
                throw new IllegalArgumentException("Invalid format: " + piece);
            }
        } else {
            return new Gson().fromJson(piece, ChessPiece.class);
        }
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return Rules.pieceRule(type).moves(board, myPosition);
    }
}
