package websocket.commands;

import model.GameData;

public class MakeMoveCommand extends UserGameCommand{
    private final GameData gameData;

    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, GameData game) {
        super(commandType, authToken, gameID);
        this.gameData = game;
    }
}
