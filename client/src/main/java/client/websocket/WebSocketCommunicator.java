package client.websocket;

import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameMessage;
import websocket.commands.HighlightMovesCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketCommunicator extends Endpoint {
    private Session session;
    private NotificationHandler notificationHandler;

    public WebSocketCommunicator(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    private void handleMessage(String message) {
        char messageType = message.split("serverMessageType")[1].charAt(3);
        switch (messageType) {
            case 'N' -> {
                handleNotification(message);
            }
            case 'E' -> {
                handleError(message);
            }
            case 'L' -> {
                handleLoadGame(message);
            }
        }
    }

    private void handleNotification(String message) {
        Notification notification = new Gson().fromJson(message, Notification.class);
        notificationHandler.notify(notification.getMessage());
    }

    private void handleError(String message) {
        ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
        notificationHandler.notifyError(errorMessage.getMessage());
    }

    private void handleLoadGame(String message) {
        LoadGameMessage loadGameMessage = new Gson().fromJson(message, LoadGameMessage.class);
        GameMessage gameInfo = loadGameMessage.getGame();
        notificationHandler.printBoard(gameInfo.boardString());
    }

    public void connectGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resignGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void redrawBoard(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.DRAW, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void highlightMoves(String authToken, int gameID, String square) throws ResponseException {
        try {
            ChessPosition pos = parsePos(square);
            var command = new HighlightMovesCommand(UserGameCommand.CommandType.HIGHLIGHT, authToken, gameID, pos);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private ChessPosition parsePos(String square) {
        char colChar = square.charAt(0);
        char rowChar = square.charAt(1);
        int col = parseCol(colChar);
        int row = parseRow(Integer.parseInt(String.valueOf(rowChar)));
        return new ChessPosition(row, col);
    }

    private int parseCol(char colChar) {
        switch(colChar) {
            case 'a' -> {
                return 1;
            }
            case 'b' -> {
                return 2;
            }
            case 'c' -> {
                return 3;
            }
            case 'd' -> {
                return 4;
            }
            case 'e' -> {
                return 5;
            }
            case 'f' -> {
                return 6;
            }
            case 'g' -> {
                return 7;
            }
            case 'h' -> {
                return 8;
            }
        }
        return 0;
    }

    private int parseRow(int rowInt) {
        return rowInt;
    }

}
