package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();

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

    public void send(Session currentSession, ServerMessage serverMessage) throws IOException {
        var c = connections.get(currentSession);
        c.send(serverMessage.toString());
    }
}

