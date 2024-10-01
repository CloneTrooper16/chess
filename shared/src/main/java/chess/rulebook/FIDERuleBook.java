package chess.rulebook;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class FIDERuleBook extends RuleBook{
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return new HashSet<>();
    }

    public boolean isBoardValid(ChessBoard board) {
        return false;
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        return false;
    }

    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        return false;
    }

    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        return false;
    }
}
