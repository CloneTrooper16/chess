package handler;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.ConnectionManager;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), command.getAuthToken(), session); //TODO: update to username
            case MAKE_MOVE -> exit(command.getCommandType());
        }

        System.out.printf("Received: %s\n", message);
        session.getRemote().sendString(new Gson().toJson(new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Testing45")));
    }

    private void connect(int gameID, String username, Session session) throws IOException {
        connections.add(username, session);
        var message = String.format("%s has joined the game as...", username);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "testing123");
        connections.broadcast(username, notification);
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

}
