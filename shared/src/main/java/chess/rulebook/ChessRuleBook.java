package chess.rulebook;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface ChessRuleBook {
    Collection<ChessMove> validMoves(ChessPosition startPosition);
    boolean isBoardValid(ChessBoard board);
    boolean isInCheck(ChessGame.TeamColor teamColor, ChessBoard board);
    boolean isInCheckmate(ChessGame.TeamColor teamColor);
    boolean isInStalemate(ChessGame.TeamColor teamColor);
}
