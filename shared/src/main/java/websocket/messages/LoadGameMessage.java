package websocket.messages;

import model.GameData;
import model.GameMessage;

public class LoadGameMessage extends ServerMessage{
    private final GameMessage game;

    public LoadGameMessage(ServerMessageType type, GameMessage game) {
        super(type);
        this.game = game;
    }

    public GameMessage getGame() {
        return game;
    }

    public String getMessage() {
        return game.toString();
    }
}