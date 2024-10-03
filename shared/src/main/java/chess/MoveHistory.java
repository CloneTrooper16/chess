package chess;

import java.util.ArrayList;
import java.util.List;

public class MoveHistory {
    private final List<MadeMove> moves = new ArrayList<>();
    //Need piece and move, also castling. perhaps track check/captures as well

    public void recordMove(ChessMove move, MadeMove.MovedPiece piece, MadeMove.MoveType type) {
        moves.add(new MadeMove(piece, move, type));
    }

    public MadeMove getMove(int index) {
        return moves.get(index);
    }
}
