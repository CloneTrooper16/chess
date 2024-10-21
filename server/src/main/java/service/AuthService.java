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

    public void deleteAllAuths() throws DataAccessException {
        authDataAccess.deleteAllAuths();
    }

    static String generateToken() {
        return UUID.randomUUID().toString();
    }


}
