package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage{
    private final String message;

    public LoadGameMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

//TODO: change to GameData object...