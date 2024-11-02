package dataaccess;

import model.AuthData;

public interface AuthDAO {

    AuthData addAuth(AuthData a) throws ServerException;
    AuthData getAuth(String authToken) throws ServerException;
    AuthData getAuthByUsername(String username) throws ServerException;
    void deleteAuth(AuthData authData) throws ServerException;

    void deleteAllAuths() throws ServerException;
}
