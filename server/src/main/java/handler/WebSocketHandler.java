package handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.ConnectionManager;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthService authService;
    private final GameService gameService;

    public WebSocketHandler() throws ServerException {
        AuthDAO authDataAccess = new DatabaseAuthDAO();
        GameDAO gameDataAccess = new DatabaseGameDAO();
        this.authService = new AuthService(authDataAccess);
        this.gameService = new GameService(gameDataAccess, authDataAccess);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, ServerException {
        int ctIndex = message.indexOf("commandType");
        char commandType = message.charAt(ctIndex + 14);
        if (commandType != 'M') {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT ->
                        connect(command.getGameID(), command.getAuthToken(), session); //TODO: update to username
                case MAKE_MOVE -> exit(command.getCommandType());
            }
        } else {
            MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
        }

        System.out.printf("Received: %s\n", message);
    }

    private void connect(int gameID, String authToken, Session session) throws IOException, ServerException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        String username = auth.username();
        String color = getPlayerColor(game, username);
        connections.add(gameID, username, session);
        var message = String.format("%s has joined the game as %s", username, color);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, username, notification);
    }

    private void exit(UserGameCommand.CommandType visitorName) throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var notification = new Notification(Notification.Type.DEPARTURE, message);
//        connections.broadcast(visitorName, notification);
    }

//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast("", notification);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }

    private String getPlayerColor(GameData game, String username) {
        if (game.whiteUsername().equals(username)) {
            return "white";
        }
        return "black";
    }
}
