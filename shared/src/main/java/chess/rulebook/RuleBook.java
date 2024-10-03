package chess.rulebook;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public abstract class RuleBook implements ChessRuleBook{
    public abstract Collection<ChessMove> validMoves(ChessPosition startPosition);
    public abstract boolean isBoardValid(ChessBoard board);
    public abstract boolean isInCheck(ChessGame.TeamColor teamColor);
    public abstract boolean isInCheckmate(ChessGame.TeamColor teamColor);
    public abstract boolean isInStalemate(ChessGame.TeamColor teamColor);

    public abstract void setBoard(ChessBoard board);
}
