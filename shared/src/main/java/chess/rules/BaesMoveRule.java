package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public abstract class BaesMoveRule implements MoveRule {
    protected void calculateMoves(ChessBoard board, ChessPosition pos, int[][] directions,
                                  Collection<ChessMove> moves, boolean continueMoving) {
//        for (int[] moveDirection : directions) {
//            boolean moveAvailable = true;
//            while(continueMoving && moveAvailable) {
//                if (true)
//                if (board.getPiece(pos) != null) {
//
//                }
//            }
//        }
    }

    public abstract Collection<ChessMove> moves(ChessBoard board, ChessPosition position);
}
