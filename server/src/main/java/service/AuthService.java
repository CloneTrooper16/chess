package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDataAccess;

    public AuthService(AuthDAO authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        return authDataAccess.getAuth(authToken);
    }

    public void logout(String authToken) throws DataAccessException {
        if (isValidAuth(authToken)) {
            var auth = getAuth(authToken);
            authDataAccess.deleteAuth(auth);
        } else {
            throw new DataAccessException("unauthorized");
        }
    }

    public void deleteAllAuths() throws DataAccessException {
        authDataAccess.deleteAllAuths();
    }

    static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidAuth(String authToken) throws DataAccessException {
        return authDataAccess.getAuth(authToken) != null;
    }


}
