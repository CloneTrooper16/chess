package chess.rules;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMoveRule extends BaseMoveRule{
//    @Override
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int direction,
                                  Collection<ChessMove> moves, boolean firstMove) {
        //Moves
        boolean moveAvailable = true;
//            ChessPosition newPos = new ChessPosition(pos);
        int[] moveToPosition = {pos.getRow() + direction, pos.getColumn()};
        while(moveAvailable) {
            ChessPosition newPos = new ChessPosition(moveToPosition[0], moveToPosition[1]);
            boolean emptySquare = checkSquareEmpty(board, newPos);
            if (emptySquare) {
                //square is empty
                if (movesToBackRow(newPos)) {
                    moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.ROOK));
                    moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.KNIGHT));
                } else {
                    moves.add(new ChessMove(pos, newPos));
                }
                if (firstMove) {
                    moveToPosition[0] += direction;
                    firstMove = false;
                } else {
                    moveAvailable = false;
                }
            } else {
                moveAvailable = false;
            }

        }
        //Captures
        int[][] captureDirections = {
                {direction, -1},    //Attack Left/Right
                {direction, 1},     //Attack Right/Left
        };
        for (int[] capDirection : captureDirections) {
            moveToPosition = new int[]{pos.getRow() + capDirection[0], pos.getColumn() + capDirection[1]};
            boolean onBoard = checkOnBoard(moveToPosition);

            if (onBoard) {
                ChessPosition newPos = new ChessPosition(moveToPosition[0], moveToPosition[1]);
                boolean emptySquare = checkSquareEmpty(board, newPos);
                if (!emptySquare) {
                    boolean samePieceColor = isSameColor(board, pos, newPos);
                    if (!samePieceColor) {
                        if (movesToBackRow(newPos)) {
                            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.QUEEN));
                            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.ROOK));
                            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.BISHOP));
                            moves.add(new ChessMove(pos, newPos, ChessPiece.PieceType.KNIGHT));
                        } else {
                            moves.add(new ChessMove(pos, newPos));
                        }
                    }
                }
            }
        }
    }

    private boolean movesToBackRow(ChessPosition pos) {
        return pos.getRow() == 1 || pos.getRow() == 8;
    }

    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int direction;
        boolean firstMove;
        ChessGame.TeamColor pieceColor = board.getPiece(position).getTeamColor();
        if (pieceColor == ChessGame.TeamColor.WHITE) {
            direction = 1;
            firstMove = position.getRow() == 2;
        } else {
            direction = -1;
            firstMove = position.getRow() == 7;
        }
        calculateMoves(board, position, direction, moves, firstMove);
        return moves;
    }
}
