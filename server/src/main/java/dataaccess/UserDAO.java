package dataaccess;

import model.UserData;

public interface UserDAO {

    UserData addUser(UserData u) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void deleteAllUsers() throws DataAccessException;
}
