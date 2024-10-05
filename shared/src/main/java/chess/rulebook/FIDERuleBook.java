package chess.rulebook;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class FIDERuleBook extends RuleBook{
    private final Collection<ChessMove> whiteMoves = new HashSet<>();
    private final Collection<ChessMove> blackMoves = new HashSet<>();
    private boolean whiteARookMoved = false;
    private boolean whiteHRookMoved = false;
    private boolean blackARookMoved = false;
    private boolean blackHRookMoved = false;
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private ChessBoard board;
    private final MoveHistory moveHistory = new MoveHistory();

    public FIDERuleBook(ChessBoard board) {
        this.board = board;
    }

    public void getAllMoves(ChessBoard board) {
        getAllMovesByColor(ChessGame.TeamColor.WHITE, board);
        getAllMovesByColor(ChessGame.TeamColor.BLACK, board);
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
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING) {
            Collection<ChessMove> castleMoves = getCastleMoves(movingColor, realMoves, board);
            realMoves.addAll(castleMoves);
        }
        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            Collection<ChessMove> enPassant = getEnPassantMove(movingColor, realMoves, startPosition, board);
            realMoves.addAll(enPassant);
        }


        return realMoves;
    }

    private Collection<ChessMove> getCastleMoves(ChessGame.TeamColor color, Collection<ChessMove> kingMoves, ChessBoard board) {
        if (color == ChessGame.TeamColor.WHITE) {
            return getCastleOptions(ChessGame.TeamColor.WHITE, kingMoves, board);
        } else {
            return getCastleOptions(ChessGame.TeamColor.BLACK, kingMoves, board);
        }
    }

    private Collection<ChessMove> getEnPassantMove(ChessGame.TeamColor color, Collection<ChessMove> pawnMoves, ChessPosition pawnPosition, ChessBoard board) {
        if (color == ChessGame.TeamColor.WHITE) {
            return getEnPassantOptions(ChessGame.TeamColor.WHITE, pawnMoves, pawnPosition, board);
        } else {
            return getEnPassantOptions(ChessGame.TeamColor.BLACK, pawnMoves, pawnPosition, board);
        }
    }

    public boolean isBoardValid(ChessBoard board) {
        return false;
    }

    private boolean kingInStartingPos(ChessPosition pos) {
        int col = pos.getColumn();
        int row = pos.getRow();
        return (row == 1 || row == 8) && col == 5;
    }

    private boolean kingNoMoved(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? !whiteKingMoved : !blackKingMoved;
    }

    private boolean rookANoMoved(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? !whiteARookMoved : !blackARookMoved;
    }

    private boolean rookHNoMoved(ChessGame.TeamColor color) {
        return color == ChessGame.TeamColor.WHITE ? !whiteHRookMoved : !blackHRookMoved;
    }

    private boolean emptySquare(ChessPosition square) {
        return board.getPiece(square) == null;
    }

    private Collection<ChessMove> validCastleMoves(Collection<ChessMove> castleMoves, ChessPiece movingKing) {
        Collection<ChessMove> realMoves = new HashSet<>();
        for (ChessMove move : castleMoves) {
            //copy board, make move, and test for check
            ChessBoard tempBoard = new ChessBoard(board);
            tempBoard.addPiece(move.getEndPosition(), movingKing);
            tempBoard.removePiece(move.getStartPosition());
            getAllMoves(tempBoard);
            if (!isInCheck(movingKing.getTeamColor(), tempBoard)) {
                realMoves.add(move);
            }
        }
        return realMoves;
    }

    private Collection<ChessMove> getCastleOptions(ChessGame.TeamColor teamColor, Collection<ChessMove> kingMoves, ChessBoard board) {
        Collection<ChessMove> result = new HashSet<>();
        ChessPosition kingPos = board.getPosition(new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        ChessPiece king = board.getPiece(kingPos);
        int kingCol = kingPos.getColumn();
        int kingRow = kingPos.getRow();
        if (kingNoMoved(teamColor) && kingInStartingPos(kingPos)) {
            if (!isInCheck(teamColor, board)) {
                boolean kingSide = false, queenSide = false;
                Collection<ChessMove> castleMoves = new HashSet<>();
                for (ChessMove validMove : kingMoves) {
                    int moveCol = validMove.getEndPosition().getColumn();
                    int moveRow = validMove.getEndPosition().getRow();
                    if (moveCol == kingCol - 1 && moveRow == kingRow) {
                        queenSide = emptySquare(new ChessPosition(kingRow, kingCol - 1));
                    } else if (moveCol == kingCol + 1 && moveRow == kingRow) {
                        kingSide = emptySquare(new ChessPosition(kingRow, kingRow + 1));;
                    }
                }
                if (rookANoMoved(teamColor) && queenSide && emptySquare(new ChessPosition(kingRow, 3))) {
                    castleMoves.add(new ChessMove(kingPos, new ChessPosition(kingRow, kingCol - 2)));
                }
                if (rookHNoMoved(teamColor) && kingSide && emptySquare(new ChessPosition(kingRow, 7))) {
                    castleMoves.add(new ChessMove(kingPos, new ChessPosition(kingRow, kingCol + 2)));
                }
                result.addAll(validCastleMoves(castleMoves, king));
            }
        }
        return result;
    }

    private Collection<ChessMove> validEnPassantMove(Collection<ChessMove> enPassantMove, ChessPiece movingPawn, ChessPosition capturedPos) {
        Collection<ChessMove> realMoves = new HashSet<>();
        for (ChessMove move : enPassantMove) {
            //copy board, make move, and test for check
            ChessBoard tempBoard = new ChessBoard(board);
            tempBoard.addPiece(move.getEndPosition(), movingPawn);
            tempBoard.removePiece(move.getStartPosition());
            tempBoard.removePiece(capturedPos);
            getAllMoves(tempBoard);
            if (!isInCheck(movingPawn.getTeamColor(), tempBoard)) {
                realMoves.add(move);
            }
        }
        return realMoves;
    }

    private Collection<ChessMove> getEnPassantOptions(ChessGame.TeamColor color, Collection<ChessMove> pawnMoves, ChessPosition pawnPos, ChessBoard board) {
        Collection<ChessMove> result = new HashSet<>();
        MadeMove lastMove = moveHistory.getLastMove();
        if (lastMove != null) {
            if (lastMove.getPiece() == MadeMove.MovedPiece.PAWN) {
                int lastMoveStartRow = lastMove.getMove().getStartPosition().getRow();
                int lastMoveEndRow = lastMove.getMove().getEndPosition().getRow();
                if (Math.abs(lastMoveEndRow - lastMoveStartRow) > 1) {
                    int pawnRow = pawnPos.getRow();
                    int pawnCol = pawnPos.getColumn();
                    int lastMoveEndCol = lastMove.getMove().getEndPosition().getColumn();
                    if (pawnRow == lastMoveEndRow && (Math.abs(pawnCol - lastMoveEndCol) == 1)) {
                        Collection<ChessMove> enPassantMove = new HashSet<>();
                        ChessPiece pawn = board.getPiece(pawnPos);
                        if (color == ChessGame.TeamColor.WHITE) {
                            enPassantMove.add(new ChessMove(pawnPos, new ChessPosition(pawnRow + 1, lastMoveEndCol)));
                        } else {
                            enPassantMove.add(new ChessMove(pawnPos, new ChessPosition(pawnRow - 1, lastMoveEndCol)));
                        }
                        result.addAll(validEnPassantMove(enPassantMove, pawn, lastMove.getMove().getEndPosition()));
                    }
                }
            }
        }
        return result;
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

    private boolean CheckForMoves(ChessGame.TeamColor teamColor) {
        ChessPosition checkedKingPos = board.getPosition(new ChessPiece(teamColor, ChessPiece.PieceType.KING));
        ChessPiece checkedKing = board.getPiece(checkedKingPos);
        Collection<ChessPosition> allPiecesOfColor = board.getAllPiecesByColor(teamColor);
        Collection<ChessMove> allValidMoves = new HashSet<>();
        for (ChessPosition piece : allPiecesOfColor) {
            allValidMoves.addAll(validMoves(piece));
        }
        return allValidMoves.isEmpty();
    }

    public boolean isInCheckmate(ChessGame.TeamColor teamColor) {
        if (!isInCheck(teamColor, board)) return false;
        return CheckForMoves(teamColor);
    }

    public boolean isInStalemate(ChessGame.TeamColor teamColor) {
        if (isInCheck(teamColor, board)) return false;
        return CheckForMoves(teamColor);
    }

    public void recordMove(ChessGame.TeamColor color, ChessMove move, MadeMove.MovedPiece piece, MadeMove.MoveType type) {
        moveHistory.recordMove(color, move, piece, type);
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public void setWhiteARookMoved(boolean val) {
        whiteARookMoved = val;
    }

    public void setWhiteHRookMoved(boolean val) {
        whiteHRookMoved = val;
    }

    public void setBlackARookMoved(boolean val) {
        blackARookMoved = val;
    }

    public void setBlackHRookMoved(boolean val) {
        blackHRookMoved = val;
    }

    public void setWhiteKingMoved(boolean val) {
        whiteKingMoved = val;
    }

    public void setBlackKingMoved(boolean val) {
        blackKingMoved = val;
    }

    public void resetMovedPieces() {
        whiteARookMoved = false;
        whiteHRookMoved = false;
        blackARookMoved = false;
        blackHRookMoved = false;
        whiteKingMoved = false;
        blackKingMoved = false;
    }
}
