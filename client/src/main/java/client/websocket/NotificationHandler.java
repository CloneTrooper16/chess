package client.websocket;

import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(String message);
    void printBoard(String board);
    void notifyError(String message);
}
