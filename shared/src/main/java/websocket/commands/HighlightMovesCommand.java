package websocket.commands;

import chess.ChessPosition;

public class HighlightMovesCommand extends UserGameCommand{
    private final ChessPosition pos;

    public HighlightMovesCommand(CommandType commandType, String authToken, Integer gameID, ChessPosition pos) {
        super(commandType, authToken, gameID);
        this.pos = pos;
    }

    public ChessPosition getPos() {
        return pos;
    }
}
