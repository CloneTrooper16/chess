package chess.rules;

import chess.ChessPiece;

import java.util.HashMap;

import static chess.ChessPiece.PieceType;
import static chess.ChessPiece.PieceType.*;

public class Rules {
    static private final HashMap<ChessPiece.PieceType, MoveRule> rules = new HashMap<>();

    static {
        rules.put(KING, new KingMoveRule());
        rules.put(QUEEN, new QueenMoveRule());
        rules.put(KNIGHT, new KnightMoveRule());
        rules.put(BISHOP, new BishopMoveRule());
        rules.put(ROOK, new RookMoveRule());
//        rules.put(PAWN, new PawnMovementRule());
    }

    static public MoveRule pieceRule(PieceType type) {
        return rules.get(type);
    }
}
