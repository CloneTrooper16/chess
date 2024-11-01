package dataaccess;

import model.UserData;

public interface UserDAO {

    UserData addUser(UserData u) throws ServerException;
    UserData getUser(String username) throws ServerException;
    void deleteAllUsers() throws ServerException;
}
