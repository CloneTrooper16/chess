package chess;

public class MadeMove {
    private final ChessMove move;
    private final MovedPiece piece;
    private final MoveType type;

    MadeMove(MovedPiece piece, ChessMove move, MoveType type) {
        this.piece = piece;
        this.move = move;
        this.type = type;
    }

    MadeMove(MovedPiece piece, ChessMove move) {
        this(piece, move, null);
    }

    public enum MovedPiece {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN,
        KINGSIDECASTLE,
        QUEENSIDECASTE,
    }

    public enum MoveType {
        CHECK,
        CAPTURE,
    }

    public ChessMove getMove() {
        return this.move;
    }

    public MovedPiece getPiece() {
        return this.piece;
    }

    public MoveType getType() {
        return this.type;
    }

}
