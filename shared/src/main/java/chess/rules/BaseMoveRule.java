package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;


public abstract class BaseMoveRule implements MoveRule {
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int[][] directions,
                                  Collection<ChessMove> moves, boolean continueMoving) {
        for (int[] moveDirection : directions) {
            boolean moveAvailable = true;
            while(moveAvailable) {
                int[] moveToPosition = {pos.getRow() + moveDirection[0], pos.getColumn() + moveDirection[1]};
                boolean onBoard = checkOnBoard(moveToPosition);

                if (onBoard) {
                    ChessPosition newPos = new ChessPosition(moveToPosition[0], moveToPosition[1]);
                    boolean emptySquare = checkSquareEmpty(board, newPos);
                    if (emptySquare) {
                        //square is empty
                        moves.add(new ChessMove(pos, newPos));
                    } else {
                        boolean samePieceColor = isSameColor(board, pos, newPos);
                        if (!samePieceColor) {
                            //opposing piece occupies the square.
                            moves.add(new ChessMove(pos, newPos));
                        }
                        //stop making new options in this direction
                        moveAvailable = false;
                    }
                    if (!continueMoving) {
                        moveAvailable = false;
                    }
                } else {
                    moveAvailable = false;
                }
            }
        }
    }

    private boolean checkOnBoard(int[] pos) {
        return pos[0] > 0 && pos[0] < 9 && pos[1] > 0 && pos[1] < 9;
    }

    private boolean checkSquareEmpty(ChessBoard board, ChessPosition pos) {
        return board.getPiece(pos) != null;
    }

    private boolean isSameColor(ChessBoard board, ChessPosition start, ChessPosition pos) {
        ChessPiece piece = board.getPiece(start);
        ChessPiece otherPiece = board.getPiece(pos);
        return piece.getTeamColor() == otherPiece.getTeamColor();
    }

    abstract public Collection<ChessMove> moves(ChessBoard board, ChessPosition position);
}
