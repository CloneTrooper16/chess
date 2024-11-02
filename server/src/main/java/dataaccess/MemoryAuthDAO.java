package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> auths = new HashMap<>();
    private final HashMap<String, AuthData> authsbyUsername = new HashMap<>();

    public AuthData addAuth(AuthData a) {
        var auth = new AuthData(a.authToken(), a.username());
        auths.put(auth.authToken(), auth);
        authsbyUsername.put(auth.username(), auth);
        return auth;
    }

    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

//Guess I didn't need the getAuthByUserName
    public AuthData getAuthByUsername(String username) {
        return authsbyUsername.get(username);
    }

    public void deleteAuth(AuthData a) {
        auths.remove(a.authToken());
        authsbyUsername.remove(a.username());
    }

    public void deleteAllAuths() {
        auths.clear();
    }

}
