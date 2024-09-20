package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class RookMoveRule extends BaseMoveRule{
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int[][] directions = {
                {1,0},      //Up
                {0,1},      //Right
                {-1,0},     //Down
                {0,-1},     //Left
        };
        calculateMoves(board, position, directions, moves, true);
        return moves;
    }
}
