package chess;

import chess.rulebook.FIDERuleBook;
import chess.rulebook.RuleBook;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;
    private final RuleBook ruleBook;


    public ChessGame() {
        this.teamTurn = TeamColor.WHITE;
        this.board = new ChessBoard();
        board.resetBoard();
        this.ruleBook = new FIDERuleBook(board);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which team's turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private TeamColor getOppositeColor(TeamColor color) {
        return color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    boolean isSpecialMove(ChessMove move, ChessPiece.PieceType pieceType) {
        if (!(pieceType == ChessPiece.PieceType.KING || pieceType == ChessPiece.PieceType.PAWN)) {
            return false;
        }
        int moveStartCol = move.getStartPosition().getColumn();
        int moveEndCol = move.getEndPosition().getColumn();
        if (pieceType == ChessPiece.PieceType.KING) {
            return moveStartCol + 2 == moveEndCol || moveStartCol - 2 == moveEndCol;
        } else {
            //piece is a pawn;
            return moveStartCol != moveEndCol && board.getPiece(move.getEndPosition()) == null;
        }
    }

    private void handleOtherPiece(ChessPiece movingPiece, ChessMove move) {
        ChessPiece.PieceType pieceType = movingPiece.getPieceType();
        TeamColor color = movingPiece.getTeamColor();
        int moveStartRow = move.getStartPosition().getRow();
        if (pieceType == ChessPiece.PieceType.KING) {
            ChessPosition movingRookPos;
            if (move.getEndPosition().getColumn() > move.getStartPosition().getColumn()) {
                //kingSide Castle
                movingRookPos = new ChessPosition(moveStartRow, 8);
                ChessPiece movingRook = board.getPiece(movingRookPos);
                board.addPiece(new ChessPosition(moveStartRow, 6), movingRook);
            } else {
                //QueenSide Castle
                movingRookPos = new ChessPosition(moveStartRow, 1);
                ChessPiece movingRook = board.getPiece(movingRookPos);
                board.addPiece(new ChessPosition(moveStartRow, 4), movingRook);
            }
            board.removePiece(movingRookPos);
        } else {
            //piece == pawn
            ChessPosition pawnPos = move.getEndPosition();
            int pawnCol = pawnPos.getColumn();
            int pawnRow = pawnPos.getRow();
            if (color == TeamColor.WHITE) {
                board.removePiece(new ChessPosition(pawnRow - 1, pawnCol));
            } else {
                board.removePiece(new ChessPosition(pawnRow + 1, pawnCol));
            }
        }
    }

    private void setPieceMoved(ChessMove move, ChessPiece movingPiece) {
        ChessPiece.PieceType pieceType = movingPiece.getPieceType();
        TeamColor pieceColor = movingPiece.getTeamColor();
        if (!(pieceType == ChessPiece.PieceType.KING || pieceType == ChessPiece.PieceType.ROOK)) {
            return;
        }
        if (pieceType == ChessPiece.PieceType.KING) {
            if (pieceColor == TeamColor.WHITE) {
                ruleBook.setWhiteKingMoved(true);
            } else {
                //Piece is black
                ruleBook.setBlackKingMoved(true);
            }
        } else {
            //pieceType == ROOK
            int moveStartCol = move.getStartPosition().getColumn();
            int moveStartRow = move.getStartPosition().getRow();
            if (pieceColor == TeamColor.WHITE) {
                if (moveStartCol == 1 && moveStartRow == 1) {
                    //A rook
                    ruleBook.setWhiteARookMoved(true);
                } else if (moveStartCol == 8 && moveStartRow == 1){
                    //H rook
                    ruleBook.setWhiteHRookMoved(true);
                }
            } else {
                //Piece is black
                if (moveStartCol == 1 && moveStartRow == 8) {
                    //A rook
                    ruleBook.setBlackARookMoved(true);
                } else if (moveStartCol == 8 && moveStartRow == 8) {
                    //H rook
                    ruleBook.setBlackHRookMoved(true);
                }
            }
        }
    }

    MadeMove.MovedPiece getMovedPieceType(boolean specialMove, ChessPiece movingPiece) {
        if (specialMove) {
            return MadeMove.MovedPiece.KINGSIDECASTLE;
        } else {
            switch (movingPiece.getPieceType()) {
                case BISHOP -> {
                    return MadeMove.MovedPiece.BISHOP;
                }
                case KNIGHT -> {
                    return MadeMove.MovedPiece.KNIGHT;
                }
                case KING -> {
                    return MadeMove.MovedPiece.KING;
                }
                case PAWN -> {
                    return MadeMove.MovedPiece.PAWN;
                }
                case QUEEN -> {
                    return MadeMove.MovedPiece.QUEEN;
                }
                case ROOK -> {
                    return MadeMove.MovedPiece.ROOK;
                }
            }
        }
        return null;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return ruleBook.validMoves(startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException();
        }
        if (board.getPiece(move.getStartPosition()).getTeamColor() != teamTurn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> pieceValidMoves = validMoves(move.getStartPosition());
        if (pieceValidMoves.isEmpty()) {
            throw new InvalidMoveException();
        }
        for (ChessMove validMove : pieceValidMoves) {
            if (validMove.equals(move)) {
                ChessPiece movingPiece = board.getPiece(move.getStartPosition());
                TeamColor oppositeColor = getOppositeColor(movingPiece.getTeamColor());
                boolean specialMove = isSpecialMove(move, movingPiece.getPieceType());
                MadeMove.MovedPiece movedPieceType = getMovedPieceType(specialMove, movingPiece);

                if (move.getPromotionPiece() == null) {
                    board.addPiece(move.getEndPosition(), movingPiece);
                } else {
                    ChessPiece newPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
                    board.addPiece(move.getEndPosition(), newPiece);
                }
                board.removePiece(move.getStartPosition());
                if (specialMove) {
                    handleOtherPiece(movingPiece, move);
                }
                ruleBook.recordMove(movingPiece.getTeamColor(), move, movedPieceType, null);
                setPieceMoved(move, movingPiece);
                setTeamTurn(oppositeColor);
                return;
            }
        }
        throw new InvalidMoveException();
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return ruleBook.isInCheck(teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return ruleBook.isInCheckmate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return ruleBook.isInStalemate(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        ruleBook.setBoard(board);
        ruleBook.resetMovedPieces();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
