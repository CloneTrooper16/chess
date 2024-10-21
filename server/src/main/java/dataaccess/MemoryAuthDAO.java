package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> auths = new HashMap<>();

    public AuthData addAuth(AuthData a) {
        var auth = new AuthData(a.authToken(), a.username());
        auths.put(auth.authToken(), auth);

        return auth;
    }

    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void deleteAllAuths() {
        auths.clear();
    }

}
