package client;

import chess.ChessGame;
import client.websocket.NotificationHandler;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import client.facade.ServerFacade;

import javax.management.NotificationFilter;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static NotificationHandler notificationHandler;

    @BeforeAll
    public static void init() throws ResponseException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port, notificationHandler);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws ResponseException {
        facade.clear();
    }

    @Test
    void register() throws ResponseException {
        var authData = registerUser();
        Assertions.assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerFail() throws ResponseException {
        var authData = registerUser();
        Assertions.assertNotEquals(authData.authToken() + "badStuff", authData.authToken());
    }

    @Test
    void login() throws ResponseException {
        var authData = registerUser();
        facade.logout(authData.authToken());
        var authData2 = facade.login(new UserData("player1", "password", "p1@email.com"));
        Assertions.assertNotEquals(authData, authData2);
    }

    @Test
    void loginFails() throws ResponseException {
        var authData = registerUser();
        facade.logout(authData.authToken());
        Assertions.assertThrows(ResponseException.class,
                () -> facade.login(new UserData("player1", "badStuff", "p1@email.com")));
    }

    @Test
    void logout() throws ResponseException {
        var authData = registerUser();
        Assertions.assertDoesNotThrow(() -> facade.logout(authData.authToken()));
    }

    @Test
    void logoutFails() throws ResponseException {
        var authData = registerUser();
        Assertions.assertThrows(ResponseException.class, () -> facade.logout(authData.authToken() + "badStuff"));
    }

    @Test
    void createGame() throws ResponseException {
        var authData = registerUser();
        int gameID = facade.createGame(authData.authToken(), "testGame");
        Assertions.assertNotEquals(0, gameID);
    }

    @Test
    void createGameFail() throws ResponseException {
        var authData = registerUser();
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame(authData.authToken() + "badStuff", "testGame"));
    }

    @Test
    void listGames() throws ResponseException {
        var authData = registerUser();
        var gameID = createTestGame(authData);
        Assertions.assertDoesNotThrow(() -> facade.listGames(authData.authToken()));
    }

    @Test
    void listGamesFail() throws ResponseException {
        var authData = registerUser();
        var gameID = createTestGame(authData);
        Assertions.assertThrows(ResponseException.class,() -> facade.listGames(authData.authToken() + "badStuff"));
    }

    @Test
    void joinGame() throws ResponseException {
        var authData = registerUser();
        var gameID = createTestGame(authData);
        Assertions.assertDoesNotThrow(() -> facade.joinGame(authData.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void joinGameFail() throws ResponseException {
        var authData = registerUser();
        var gameID = createTestGame(authData);
        facade.joinGame(authData.authToken(), ChessGame.TeamColor.WHITE, gameID);
        Assertions.assertThrows(ResponseException.class,() -> facade.joinGame(authData.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    void clearAll() throws ResponseException {
        Assertions.assertDoesNotThrow( () -> facade.clear());
    }

    private AuthData registerUser() throws ResponseException {
        return facade.register(new UserData("player1", "password", "p1@email.com"));
    }

    private int createTestGame(AuthData authData) throws ResponseException {
        return facade.createGame(authData.authToken(), "testGame");
    }

}
