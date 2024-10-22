package chess.rules;

import chess.ChessPiece;

import java.util.HashMap;

import static chess.ChessPiece.PieceType;
import static chess.ChessPiece.PieceType.*;

public class Rules {
    static private final HashMap<ChessPiece.PieceType, MoveRule> RULES = new HashMap<>();

    static {
        RULES.put(KING, new KingMoveRule());
        RULES.put(QUEEN, new QueenMoveRule());
        RULES.put(KNIGHT, new KnightMoveRule());
        RULES.put(BISHOP, new BishopMoveRule());
        RULES.put(ROOK, new RookMoveRule());
        RULES.put(PAWN, new PawnMoveRule());
    }

    static public MoveRule pieceRule(PieceType type) {
        return RULES.get(type);
    }
}
