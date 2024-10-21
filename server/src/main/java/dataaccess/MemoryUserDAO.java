package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO{
    private final HashMap<String,UserData> users = new HashMap<>();

    public UserData addUser(UserData u) {
        var user = new UserData(u.username(), u.password(), u.email());
        users.put(user.username(), user);

        return user;
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void deleteAllUsers() {
        users.clear();
    }
}
