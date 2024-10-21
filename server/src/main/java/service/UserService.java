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
        var newUser = userDataAccess.addUser(user);
        String authToken = AuthService.generateToken();
        AuthData newAuth = new AuthData(authToken, newUser.username());
        return authDataAccess.addAuth(newAuth);
    }
    public AuthData login(UserData user) {
        return new AuthData("dummy", "data");
    }
    public void logout(AuthData auth) {}
}
