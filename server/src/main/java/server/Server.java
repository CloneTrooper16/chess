package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import handler.Handler;
import model.UserData;
import service.AuthService;
import service.UserService;
import spark.*;

public class Server {
    private final Handler handler = new Handler();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
//        Spark.get("/game", this::listGames);

        Spark.post("/user", handler::registerUser);
//        Spark.post("/session", handler::loginUser);
//        Spark.post("/game", this::createGame);
//
//        Spark.put("/game", this::joinGame);
//
//        Spark.delete("/session", this::logoutUser);
        Spark.delete("/db", handler::deleteDB);

        Spark.exception(DataAccessException.class, handler::exceptionHandler);

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
