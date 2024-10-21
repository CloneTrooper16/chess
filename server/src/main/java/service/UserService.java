package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

public class UserService {
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public UserService(UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }
    public AuthData register(UserData user) throws DataAccessException {
        if (userDataAccess.getUser(user.username()) != null) {
            throw new DataAccessException("already taken");
        }
        if (user.password() == null || user.password().isEmpty()) {
            throw new DataAccessException("bad request");
        }
        var newUser = userDataAccess.addUser(user);
        String authToken = AuthService.generateToken();
        AuthData newAuth = new AuthData(authToken, newUser.username());
        return authDataAccess.addAuth(newAuth);
    }
    public AuthData login(UserData user) throws DataAccessException {
        var userInfo = getUser(user.username());
        if (userInfo != null) {
            if (user.password().equals(userInfo.password())) {
                if (authDataAccess.getAuthByUsername(user.username()) == null) {
                    String authToken = AuthService.generateToken();
                    AuthData newAuth = new AuthData(authToken, user.username());
                    return authDataAccess.addAuth(newAuth);
                }
            }
        }
        throw new DataAccessException("unauthorized");
    }

    public UserData getUser(String username) throws DataAccessException {
        return userDataAccess.getUser(username);
    }

    public void deleteAllUsers() throws DataAccessException {
        userDataAccess.deleteAllUsers();
    }
}
