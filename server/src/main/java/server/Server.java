package server;

import dataaccess.ServerException;
import handler.Handler;
import handler.WebSocketHandler;
import spark.*;

public class Server {
    private final Handler handler;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        try {
            handler = new Handler();
            webSocketHandler = new WebSocketHandler();
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", webSocketHandler);

        // Register your endpoints and handle exceptions here.
        Spark.get("/game", handler::listGames);

        Spark.post("/user", handler::registerUser);
        Spark.post("/session", handler::loginUser);
        Spark.post("/game", handler::createGame);

        Spark.put("/game", handler::joinGame);

        Spark.delete("/session", handler::logoutUser);
        Spark.delete("/db", handler::deleteDB);

        Spark.exception(ServerException.class, handler::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

}
