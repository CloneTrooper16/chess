package chess.rulebook;

import chess.*;

import java.util.Collection;

public abstract class RuleBook implements ChessRuleBook{
    public abstract Collection<ChessMove> validMoves(ChessPosition startPosition);
    public abstract boolean isBoardValid(ChessBoard board);
    public abstract boolean isInCheck(ChessGame.TeamColor teamColor, ChessBoard board);
    public abstract boolean isInCheckmate(ChessGame.TeamColor teamColor);
    public abstract boolean isInStalemate(ChessGame.TeamColor teamColor);

    public abstract void recordMove(ChessGame.TeamColor color, ChessMove move, MadeMove.MovedPiece piece, MadeMove.MoveType type);
    public abstract void setBoard(ChessBoard board);
    public abstract void setWhiteARookMoved(boolean val);
    public abstract void setWhiteHRookMoved(boolean val);
    public abstract void setBlackARookMoved(boolean val);
    public abstract void setBlackHRookMoved(boolean val);
    public abstract void setWhiteKingMoved(boolean val);
    public abstract void setBlackKingMoved(boolean val);
    public abstract void resetMovedPieces();
}
