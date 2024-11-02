package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DataAccessTests {
    private static final AuthDAO AUTH_DATA_ACCESS;

    static {
        try {
            AUTH_DATA_ACCESS = new DatabaseAuthDAO();
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }

    private static final GameDAO GAME_DATA_ACCESS;

    static {
        try {
            GAME_DATA_ACCESS = new DatabaseGameDAO();
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }

    private static final UserDAO USER_DATA_ACCESS;

    static {
        try {
            USER_DATA_ACCESS = new DatabaseUserDAO();
        } catch (ServerException e) {
            throw new RuntimeException(e);
        }
    }


    @BeforeEach
    void clear() throws ServerException {
        AUTH_DATA_ACCESS.deleteAllAuths();
        USER_DATA_ACCESS.deleteAllUsers();
        GAME_DATA_ACCESS.clear();
    }

    @Test
    void addUser() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var madeUser = USER_DATA_ACCESS.getUser(userInfo.username());
        assertNotNull(madeUser);
    }

    @Test
    void addUserFail() throws ServerException {
        var user = new UserData("", "", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var madeUser = USER_DATA_ACCESS.getUser(userInfo.username() + "badstuff");
        assertNull(madeUser);
    }

    @Test
    void getUser() throws ServerException {
        var user = new UserData("john Steven doe2", "password3", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var madeUser = USER_DATA_ACCESS.getUser(userInfo.username());
        assertNotNull(madeUser);
    }

    @Test
    void getUserFail() throws ServerException {
        var user = new UserData("john doe5", "password1testing", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var madeUser = USER_DATA_ACCESS.getUser(userInfo.username() + "badstuff");
        assertNull(madeUser);
    }

    @Test
    void deleteAllUsers() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        USER_DATA_ACCESS.deleteAllUsers();

        var madeUser = USER_DATA_ACCESS.getUser(userInfo.username());
        assertNull(madeUser);
    }

    @Test
    void addAuth() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken());
        assertNotNull(madeAuth);
    }

    @Test
    void addAuthFail() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken() + "badstuff");
        assertNull(madeAuth);
    }

    @Test
    void getAuth() throws ServerException {
        var auth = new AuthData("tokentokentoken", "usernamedname");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken());
        assertNotNull(madeAuth);
    }

    @Test
    void getAuthFail() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken() + "badStuff");
        assertNull(madeAuth);
    }

    @Test
    void getAuthByUsername() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuthByUsername(authInfo.username());
        assertNotNull(madeAuth);
    }

    @Test
    void getAuthByUsernameFail() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        var madeAuth = AUTH_DATA_ACCESS.getAuthByUsername(authInfo.username() + "b");
        assertNull(madeAuth);
    }

    @Test
    void deleteAuth() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        AUTH_DATA_ACCESS.deleteAuth(authInfo);

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken());
        assertNull(madeAuth);
    }

    @Test
    void deleteAuthFail() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);


        AUTH_DATA_ACCESS.deleteAuth(new AuthData("token2", "username"));

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken());
        assertNotNull(madeAuth);
    }

    @Test
    void deleteAllAuth() throws ServerException {
        var auth = new AuthData("token", "username");
        var authInfo = AUTH_DATA_ACCESS.addAuth(auth);

        AUTH_DATA_ACCESS.deleteAllAuths();

        var madeAuth = AUTH_DATA_ACCESS.getAuth(authInfo.authToken());
        assertNull(madeAuth);
    }

    @Test
    void createGame() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("test");
        assertNotNull(gameID);
    }

    @Test
    void createGameFail() throws ServerException {
        var user = new UserData("john doe done gone", "passwordtest", "example@mail.net");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("test");
        assertNotNull(gameID);
    }

    @Test
    void listGames() throws ServerException {
        var user = new UserData("john joseph", "password", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("rutabaga");
        assertNotNull(gameID);
    }

    @Test
    void listGamesFail() throws ServerException {
        var user = new UserData("john doe testing 123", "password lolz", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("right");
        assertNotNull(gameID);
    }

    @Test
    void getGame() throws ServerException {
        var user = new UserData("john doe test", "password12345", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("left");
        assertNotNull(gameID);
    }

    @Test
    void getGameFail() throws ServerException {
        var user = new UserData("doe john", "wordpass", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("tset");
        assertNotNull(gameID);
    }

    @Test
    void updateGame() throws ServerException {
        var user = new UserData("john doeI'mdown", "passwordhelp", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("forwards");
        assertNotNull(gameID);
    }

    @Test
    void updateGameFail() throws ServerException {
        var user = new UserData("john doeFail", "passwordFail", "example@mail.com");
        var userInfo = USER_DATA_ACCESS.addUser(user);

        var gameID = GAME_DATA_ACCESS.createGame("backwards");
        assertNotNull(gameID);
    }
}
