package websocket;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import model.GameMessage;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import websocket.messages.LoadGameMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static chess.ChessGame.TeamColor.BLACK;
import static chess.ChessGame.TeamColor.WHITE;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();
    private final GameService gameService;

    public ConnectionManager() throws ServerException {
        AuthDAO authDataAccess = new DatabaseAuthDAO();
        GameDAO gameDataAccess = new DatabaseGameDAO();
        this.gameService = new GameService(gameDataAccess, authDataAccess);
    }

    public void add(int gameID, String username, Session session) {
        var connection = new Connection(username, session, gameID);
        connections.put(session, connection);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session currentSession, String excludeUsername, ServerMessage serverMessage) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                boolean sameGame = connections.get(c.session).gameID == connections.get(currentSession).gameID;
                if (!c.username.equals(excludeUsername) && sameGame) {
                    c.send(serverMessage.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.session);
        }
    }

    public void broadcastNewBoard(Session currentSession, LoadGameMessage loadGameMessage) throws IOException, ServerException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.values()) {
            if (c.session.isOpen()) {
                int gameID = connections.get(c.session).gameID;
                String username = connections.get(c.session).username;
                boolean sameGame = gameID == connections.get(currentSession).gameID;
                if (sameGame) {
                    ChessGame.TeamColor tColor = getTeamColor(username, gameID);
                    String[] boards = loadGameMessage.getGame().boardString().split("SPLITTER");
                    LoadGameMessage newLoadGameMessage;
                    if (tColor == BLACK) {
                        GameMessage newGameMessage = new GameMessage(gameID, tColor, boards[1]);
                        newLoadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGameMessage);
                    } else {
                        GameMessage newGameMessage = new GameMessage(gameID, tColor, boards[0]);
                        newLoadGameMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, newGameMessage);
                    }
                    c.send(newLoadGameMessage.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.session);
        }
    }

    public void send(Session currentSession, ServerMessage serverMessage) throws IOException {
        var c = connections.get(currentSession);
        c.send(serverMessage.toString());
    }

    private ChessGame.TeamColor getTeamColor(String username, int gameID) throws ServerException {
        GameData game = gameService.getGame(gameID);
        if (game.blackUsername() != null && game.blackUsername().equals(username)) {
            return BLACK;
        } else {
            return WHITE;
        }
    }
}

