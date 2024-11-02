package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.ServerException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDataAccess;
    private final AuthDAO authDataAccess;

    public UserService(UserDAO userDataAccess, AuthDAO authDataAccess) {
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
    }
    public AuthData register(UserData user) throws ServerException {
        if (userDataAccess.getUser(user.username()) != null) {
            throw new ServerException("already taken");
        }
        if (user.password() == null || user.password().isEmpty()) {
            throw new ServerException("bad request");
        }
        var newUser = userDataAccess.addUser(user);
        String authToken = AuthService.generateToken();
        AuthData newAuth = new AuthData(authToken, newUser.username());
        return authDataAccess.addAuth(newAuth);
    }
    public AuthData login(UserData user) throws ServerException {
        var userInfo = getUser(user.username());
        if (userInfo != null) {
            AuthData auth = authDataAccess.getAuthByUsername(user.username());
            boolean alreadyLoggedIn = auth != null;
            if (!alreadyLoggedIn) {
                if (BCrypt.checkpw(user.password(), userInfo.password())) {
                    String authToken = AuthService.generateToken();
                    AuthData newAuth = new AuthData(authToken, user.username());
                    return authDataAccess.addAuth(newAuth);
                }
            } else {
                return auth;
            }
        }
        throw new ServerException("unauthorized");
    }

    public UserData getUser(String username) throws ServerException {
        return userDataAccess.getUser(username);
    }

    public void deleteAllUsers() throws ServerException {
        userDataAccess.deleteAllUsers();
    }
}
