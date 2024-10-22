package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class Handler {
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public Handler() {
        UserDAO userDataAccess = new MemoryUserDAO();
        AuthDAO authDataAccess = new MemoryAuthDAO();
        GameDAO gameDataAccess = new MemoryGameDAO();
        this.userService = new UserService(userDataAccess, authDataAccess);
        this.authService = new AuthService(authDataAccess);
        this.gameService = new GameService(gameDataAccess, authDataAccess);
    }

    public Object registerUser(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.register(user);
        return new Gson().toJson(auth);
    }

    public Object loginUser(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.login(user);
        return new Gson().toJson(auth);
    }

    public Object createGame(Request req, Response res) throws DataAccessException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        CreateGameRequest createGameRequest = new Gson().fromJson(req.body(), CreateGameRequest.class);
        int gameID = gameService.createGame(auth, createGameRequest.gameName);
        Map<String, Integer> jsonMap = new HashMap<>();
        jsonMap.put("gameID", gameID);

        return new Gson().toJson(jsonMap);
    }

    public Object joinGame(Request req, Response res) throws DataAccessException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        JoinGameRequest joinGameRequest = new Gson().fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(auth, joinGameRequest);

        return new Gson().toJson(new Object());
    }

    public Object logoutUser(Request req, Response res) throws DataAccessException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        authService.logout(auth);
        return new Gson().toJson(new Object());
    }

    public Object deleteDB(Request req, Response res) throws DataAccessException {
        userService.deleteAllUsers();
        authService.deleteAllAuths();
        return new Gson().toJson(new Object());
    }

    public void exceptionHandler(DataAccessException ex, Request req, Response res) {
        if (ex.getMessage().equals("already taken")) {
            res.status(403);
        } else if (ex.getMessage().equals("unauthorized")) {
            res.status(401);
        } else {
            res.status(400);
        }

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("message", "error: " + ex.getMessage());

        res.body( new Gson().toJson(jsonMap));
    }

    public record CreateGameRequest(String gameName) {}
    public record JoinGameRequest(ChessGame.TeamColor playerColor, int gameID) {}
}
