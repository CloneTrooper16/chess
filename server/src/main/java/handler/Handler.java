package handler;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.Request;
import spark.Response;

import javax.xml.crypto.Data;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Handler {
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;

    public Handler() throws ServerException {
        UserDAO userDataAccess = new DatabaseUserDAO();
        AuthDAO authDataAccess = new DatabaseAuthDAO();
        GameDAO gameDataAccess = new DatabaseGameDAO();
        this.userService = new UserService(userDataAccess, authDataAccess);
        this.authService = new AuthService(authDataAccess);
        this.gameService = new GameService(gameDataAccess, authDataAccess);
    }

    public Object listGames(Request req, Response res) throws ServerException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        Collection<GameData> games = gameService.listGames(auth);
        ListGamesResponse listGamesResponse = new ListGamesResponse(games);
        String json = new Gson().toJson(listGamesResponse);
        return json;
    }

    public Object registerUser(Request req, Response res) throws ServerException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.register(user);
        return new Gson().toJson(auth);
    }

    public Object loginUser(Request req, Response res) throws ServerException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.login(user);
        return new Gson().toJson(auth);
    }

    public Object createGame(Request req, Response res) throws ServerException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        CreateGameRequest createGameRequest = new Gson().fromJson(req.body(), CreateGameRequest.class);
        int gameID = gameService.createGame(auth, createGameRequest.gameName());
        CreateGameResponse createGameResponse = new CreateGameResponse(gameID);
        return new Gson().toJson(createGameResponse);
    }

    public Object joinGame(Request req, Response res) throws ServerException {
        String auth = req.headers("authorization");
        JoinGameRequest joinGameRequest = new Gson().fromJson(req.body(), JoinGameRequest.class);
        gameService.joinGame(auth, joinGameRequest);

        return new Gson().toJson(new Object());
    }

    public Object logoutUser(Request req, Response res) throws ServerException {
        var auth = new Gson().fromJson(req.headers("authorization"), String.class);
        authService.logout(auth);
        return new Gson().toJson(new Object());
    }

    public Object deleteDB(Request req, Response res) throws ServerException {
        userService.deleteAllUsers();
        authService.deleteAllAuths();
        gameService.deleteAllGames();
        return new Gson().toJson(new Object());
    }

    public void exceptionHandler(ServerException ex, Request req, Response res) {
        if (ex.getMessage().equals("already taken")) {
            res.status(403);
        } else if (ex.getMessage().equals("unauthorized")) {
            res.status(401);
        } else if (ex.getMessage().startsWith("Unable")) {
            res.status(500);
        } else {
            res.status(400);
        }

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("message", "error: " + ex.getMessage());

        res.body( new Gson().toJson(jsonMap));
    }




}
