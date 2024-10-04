package chess.rulebook;

import chess.*;

import javax.swing.*;
import java.util.Collection;
import java.util.HashSet;

public class FIDERuleBook extends RuleBook{
    private final Collection<ChessMove> whiteMoves = new HashSet<>();
    private final Collection<ChessMove> blackMoves = new HashSet<>();
    private ChessBoard board;

    public FIDERuleBook(ChessBoard board) {
        this.board = board;
    }

    public void getAllMoves(ChessBoard board) {
        whiteMoves.clear();
        blackMoves.clear();

        whiteMoves.addAll(board.getAllMovesForTeam(ChessGame.TeamColor.WHITE));
        blackMoves.addAll(board.getAllMovesForTeam(ChessGame.TeamColor.BLACK));
    }

    public void getAllMovesByColor(ChessGame.TeamColor color, ChessBoard board) {
        if (color == ChessGame.TeamColor.WHITE) {
            whiteMoves.clear();
            whiteMoves.addAll(board.getAllMovesForTeam(color));
        } else {
            blackMoves.clear();
            blackMoves.addAll(board.getAllMovesForTeam(color));
        }
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece movingPiece = board.getPiece(startPosition);
        ChessGame.TeamColor movingColor = movingPiece.getTeamColor();
        ChessGame.TeamColor oppositeColor = movingColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        Collection<ChessMove> allMoves = movingPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> realMoves = new HashSet<>();
        for (ChessMove move : allMoves) {
            //copy board, make move, and test for check?
            ChessBoard tempBoard = new ChessBoard(board);
            tempBoard.addPiece(move.getEndPosition(), movingPiece);
            tempBoard.removePiece(move.getStartPosition());
            getAllMoves(tempBoard);
            if (!isInCheck(movingColor, tempBoard)) {
                realMoves.add(move);
            }
        }

        return realMoves;
    }

    public boolean isBoardValid(ChessBoard board) {
        return false;
    }

    public boolean isInCheck(ChessGame.TeamColor teamColor, ChessBoard board) {
        ChessPosition kingPos = board.getPosition(new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        if (teamColor == ChessGame.TeamColor.WHITE) {
            getAllMovesByColor(ChessGame.TeamColor.BLACK, board);
            for (ChessMove move : blackMoves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        else {
            getAllMovesByColor(ChessGame.TeamColor.WHITE, board);
            for (ChessMove move : whiteMoves) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        if (!isInCheck(teamColor, board)) return false;
        ChessPosition checkedKingPos = board.getPosition(new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        ChessPiece checkedKing = board.getPiece(checkedKingPos);
        Collection<ChessPosition> allPiecesOfColor = board.getAllPiecesByColor(teamColor);
        Collection<ChessMove> allValidMoves = new HashSet<>();
        for (ChessPosition piece : allPiecesOfColor) {
            allValidMoves.addAll(validMoves(piece));
        }
        return allValidMoves.isEmpty();
    }

    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        return false;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }
}
