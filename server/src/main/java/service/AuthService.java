package service;

import dataaccess.AuthDAO;
import dataaccess.ServerException;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDataAccess;

    public AuthService(AuthDAO authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public AuthData getAuth(String authToken) throws ServerException {
        return authDataAccess.getAuth(authToken);
    }

    public void logout(String authToken) throws ServerException {
        if (isValidAuth(authToken)) {
            var auth = getAuth(authToken);
            authDataAccess.deleteAuth(auth);
        } else {
            throw new ServerException("unauthorized");
        }
    }

    public void deleteAllAuths() throws ServerException {
        authDataAccess.deleteAllAuths();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isValidAuth(String authToken) throws ServerException {
        return authDataAccess.getAuth(authToken) != null;
    }


}
