package service;

import chess.ChessGame;
import dataaccess.*;
import handler.Handler;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static service.AuthService.generateToken;

public class ServiceTests {
    private static final AuthDAO AUTH_DATA_ACCESS = new MemoryAuthDAO();
    static final UserService USER_SERVICE = new UserService(new MemoryUserDAO(), AUTH_DATA_ACCESS);
    static final AuthService AUTH_SERVICE = new AuthService(AUTH_DATA_ACCESS);
    static final GameService GAME_SERVICE = new GameService(new MemoryGameDAO(), AUTH_DATA_ACCESS);

    @BeforeEach
    void clear() throws ServerException {
        USER_SERVICE.deleteAllUsers();
        AUTH_SERVICE.deleteAllAuths();
    }

    @Test
    void register() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = USER_SERVICE.register(user);

        var madeUser = USER_SERVICE.getUser(user.username());
        var madeAuth = AUTH_SERVICE.getAuth(auth.authToken());
        assertNotNull(madeUser);
        assertNotNull(madeAuth);
    }

    @Test
    void noDuplicateUsers() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        USER_SERVICE.register(user);

        assertThrows(ServerException.class, () -> USER_SERVICE.register(user));
    }

    @Test
    void logout() throws ServerException {
        var auth = addUser();

        assertDoesNotThrow(() -> AUTH_SERVICE.logout(auth.authToken()));
    }

    @Test
    void logoutFail() throws ServerException {
        addUser();

        assertThrows(ServerException.class, () -> AUTH_SERVICE.logout(generateToken()));
    }

    @Test
    void login() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = USER_SERVICE.register(user);
        AUTH_SERVICE.logout(auth.authToken());

        var madeAuth = USER_SERVICE.login(user);

        assertNotNull(madeAuth);
    }

    @Test
    void loginFail() throws ServerException {
        var auth = addUser();
        AUTH_SERVICE.logout(auth.authToken());

        var badUser = new UserData("john doe", "badPassword", null);

        assertThrows(ServerException.class, () -> USER_SERVICE.login(badUser));
    }

    @Test
    void createGame() throws ServerException {
        var auth = addUser();
        int expectedID = GAME_SERVICE.getNextID();
        var id = GAME_SERVICE.createGame(auth.authToken(), "gameName");

        assertEquals(expectedID, id); //maybe change this to listGames when written;
    }

    @Test
    void createGameFail() throws ServerException {
        var auth = addUser();
        assertThrows(ServerException.class, () -> GAME_SERVICE.createGame(auth.authToken() + "badStuff", "name"));
    }

    @Test
    void getUser() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        USER_SERVICE.register(user);

        var grabbedUser = USER_SERVICE.getUser(user.username());

        assertEquals(user, grabbedUser);
    }

    @Test
    void getUserFail() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        USER_SERVICE.register(user);

        var grabbedUser = USER_SERVICE.getUser(user.username() + "badStuff");

        assertNotEquals(user, grabbedUser);
    }

    @Test
    void getAuth() throws ServerException {
        var auth = addUser();
        var grabbedAuth = AUTH_SERVICE.getAuth(auth.authToken());

        assertEquals(auth, grabbedAuth);
    }

    @Test
    void getAuthFail() throws ServerException {
        var auth = addUser();
        var grabbedAuth = AUTH_SERVICE.getAuth(auth.authToken() + "badStuff");

        assertNotEquals(auth, grabbedAuth);
    }

    @Test
    void joinGame() throws ServerException {
        var auth = addUser();
        int id = createTestGame(auth);
        assertDoesNotThrow(()-> GAME_SERVICE.joinGame(auth.authToken(), new Handler.JoinGameRequest(ChessGame.TeamColor.WHITE, id)));
    }

    @Test
    void joinGameFail() throws ServerException {
        var auth = addUser();
        int id = createTestGame(auth);
        var joinGameRequest = new Handler.JoinGameRequest(ChessGame.TeamColor.WHITE, id);
        assertThrows(ServerException.class, ()-> GAME_SERVICE.joinGame(auth.authToken() + "badStuff", joinGameRequest));
    }

    @Test
    void listGames() throws ServerException {
        var auth = addUser();
        createTestGame(auth);
        assertDoesNotThrow(()-> GAME_SERVICE.listGames(auth.authToken()));
    }

    @Test
    void listGamesFail() throws ServerException {
        var auth = addUser();
        createTestGame(auth);
        assertThrows(ServerException.class, ()-> GAME_SERVICE.listGames(auth.authToken() + "badStuff"));
    }

    AuthData addUser() throws ServerException {
        var user = new UserData("john doe", "password", "example@mail.com");
        return USER_SERVICE.register(user);
    }

    int createTestGame(AuthData auth) throws ServerException {
        return GAME_SERVICE.createGame(auth.authToken(), "gameName");
    }
}
