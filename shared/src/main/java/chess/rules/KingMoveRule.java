package chess.rules;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;

public class KingMoveRule extends BaseMoveRule{
    @Override
    public Collection<ChessMove> moves(ChessBoard board, ChessPosition position) {
        var moves = new HashSet<ChessMove>();
        int[][] directions = {
                {1,-1},     //Up to the left
                {1,0},      //Straight Up
                {1,1},      //Up to the right
                {0,1},      //Straight Right
                {-1,-1},    //Down to the left
                {-1, 0},    //Straight Down
                {-1,1},     //Down to the right
                {0,-1},     //Straight Left
        };
        calculateMoves(board, position, directions, moves, false);
        return moves;
    }
}
