package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.UserData;
import service.AuthService;
import service.UserService;
import spark.*;

public class Server {
    private final UserService userService = new UserService(new MemoryUserDAO(), new MemoryAuthDAO());
    private final AuthService authService = new AuthService();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
//        Spark.get("/game", this::listGames);

        Spark.post("/user", this::registerUser);
//        Spark.post("/session", this::loginUser);
//        Spark.post("/game", this::createGame);
//
//        Spark.put("/game", this::joinGame);
//
//        Spark.delete("/session", this::logoutUser);
//        Spark.delete("/db", this::deleteDB);

        Spark.exception(DataAccessException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(DataAccessException ex, Request req, Response res) {
        res.status(400);
    }

    private Object registerUser(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.register(user);
        return new Gson().toJson(auth);
    }
}
