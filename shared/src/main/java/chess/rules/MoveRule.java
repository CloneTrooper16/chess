package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface MoveRule {
    Collection<ChessMove> moves(ChessBoard board, ChessPosition pos);
}
