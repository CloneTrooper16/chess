package handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.UserData;
import service.AuthService;
import service.UserService;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class Handler {
    private final UserService userService;
    private final AuthService authService;

    public Handler() {
        UserDAO userDataAccess = new MemoryUserDAO();
        AuthDAO authDataAccess = new MemoryAuthDAO();
        this.userService = new UserService(userDataAccess, authDataAccess);
        this.authService = new AuthService(authDataAccess);
    }

    public Object registerUser(Request req, Response res) throws DataAccessException {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var auth = userService.register(user);
        return new Gson().toJson(auth);
    }

//    public Object loginUser(Request req, Response res) throws DataAccessException {
//
//    }

    public Object deleteDB(Request req, Response res) throws DataAccessException {
        userService.deleteAllUsers();
        authService.deleteAllAuths();
        return new Gson().toJson(new Object());
    }

    public void exceptionHandler(DataAccessException ex, Request req, Response res) {
        if (ex.getMessage().equals("already taken")) {
            res.status(403);
        } else {
            res.status(400);
        }

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("message", "error: " + ex.getMessage());

        res.body( new Gson().toJson(jsonMap));
    }
}
