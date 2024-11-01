package dataaccess;

import model.UserData;

public class DatabaseUserDAO implements UserDAO {
    public UserData addUser(UserData u) throws DataAccessException {
        return new UserData("name", "password", "email");
    }
    public UserData getUser(String username) throws DataAccessException {
        return new UserData("name", "password", "email");
    }

    public void deleteAllUsers() throws DataAccessException {

    }
}
