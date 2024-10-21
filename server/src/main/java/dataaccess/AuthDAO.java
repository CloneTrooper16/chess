package dataaccess;

import model.AuthData;

public interface AuthDAO {

    AuthData addAuth(AuthData a) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    AuthData getAuthByUsername(String username) throws DataAccessException;
    void deleteAuth(AuthData authData) throws DataAccessException;

    void deleteAllAuths() throws DataAccessException;
}

