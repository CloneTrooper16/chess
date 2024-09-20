package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class KnightMoveRule extends BaseMoveRule{
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int[][] directions = {
                {2,-1},     //Up to the left
                {2,1},      //Up to the right
                {1,2},      //Right to the up
                {-1,2},      //Right to the down
                {-2,1},    //Down to the right
                {-2,-1},    //Down to the left
                {-1,-2},     //Left to the down
                {1,-2},     //Left to the up
        };
        calculateMoves(board, position, directions, moves, false);
        return moves;
    }
}
