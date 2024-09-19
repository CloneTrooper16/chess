package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class BishopMoveRule extends BaseMoveRule{
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int[][] directions = {
                {1,-1},     //Up to the left
                {1,1},      //Up to the right
                {-1,-1},    //Down to the left
                {-1,1},     //Down to the right
        };
        calculateMoves(board, position, directions, moves, true);
        return moves;
    }
}
