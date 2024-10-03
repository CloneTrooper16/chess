package chess.rulebook;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class FIDERuleBook extends RuleBook{
    private final Collection<ChessMove> whiteMoves = new HashSet<>();
    private final Collection<ChessMove> blackMoves = new HashSet<>();
    private ChessBoard board;

    public FIDERuleBook(ChessBoard board) {
        this.board = board;
    }

    public void getAllMovesByColor(ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            whiteMoves.clear();
            whiteMoves.addAll(board.getAllMovesForTeam(color));
        } else {
            blackMoves.clear();
            blackMoves.addAll(board.getAllMovesForTeam(color));
        }
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        return new HashSet<>();
    }

    public boolean isBoardValid(ChessBoard board) {
        return false;
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPos = board.getPosition(new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        if (teamColor == ChessGame.TeamColor.WHITE) {
            getAllMovesByColor(ChessGame.TeamColor.BLACK);
            for (ChessMove move : blackMoves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        else {
            getAllMovesByColor(ChessGame.TeamColor.WHITE);
            for (ChessMove move : whiteMoves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;
        return false;
    }

    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        return false;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }
}
