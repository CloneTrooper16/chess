package dataaccess;

import model.AuthData;

public class DatabaseAuthDAO implements AuthDAO {
    public AuthData addAuth(AuthData a) throws DataAccessException {
        return new AuthData("token", "user");
    }
    public AuthData getAuth(String authToken) throws DataAccessException {
        return new AuthData("token", "user");
    }
    public void deleteAuth(AuthData authData) throws DataAccessException {

    }

    public void deleteAllAuths() throws DataAccessException {

    }
}
