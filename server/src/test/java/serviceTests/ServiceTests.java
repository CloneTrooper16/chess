package serviceTests;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.GameService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static service.AuthService.generateToken;

public class ServiceTests {
    private static final AuthDAO authDataAccess = new MemoryAuthDAO();
    static final UserService uService = new UserService(new MemoryUserDAO(), authDataAccess);
    static final AuthService aService = new AuthService(authDataAccess);
    static final GameService gService = new GameService(new MemoryGameDAO(), authDataAccess);

    @BeforeEach
    void clear() throws DataAccessException {
        uService.deleteAllUsers();
        aService.deleteAllAuths();
    }

    @Test
    void register() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = uService.register(user);

        var madeUser = uService.getUser(user.username());
        var madeAuth = aService.getAuth(auth.authToken());
        assertNotNull(madeUser);
        assertNotNull(madeAuth);
    }

    @Test
    void noDuplicateUsers() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        uService.register(user);

        assertThrows(DataAccessException.class, () -> uService.register(user));
    }

    @Test
    void logout() throws DataAccessException {
        var auth = addUser();

        assertDoesNotThrow(() -> aService.logout(auth.authToken()));
    }

    @Test
    void logoutFail() throws DataAccessException {
        addUser();

        assertThrows(DataAccessException.class, () -> aService.logout(generateToken()));
    }

    @Test
    void login() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = uService.register(user);
        aService.logout(auth.authToken());

        var madeAuth = uService.login(user);

        assertNotNull(madeAuth);
    }

    @Test
    void loginFail() throws DataAccessException {
        var auth = addUser();
        aService.logout(auth.authToken());

        var badUser = new UserData("john doe", "badPassword", null);

        assertThrows(DataAccessException.class, () -> uService.login(badUser));
    }

    @Test
    void createGame() throws DataAccessException {
        var auth = addUser();

        var id = gService.createGame(auth.authToken(), "gameName");

        assertEquals(1, id); //maybe change this to listGames when written;
    }

    @Test
    void createGameFail() throws DataAccessException {
        var auth = addUser();
        assertThrows(DataAccessException.class, () -> gService.createGame(auth.authToken() + "badStuff", "name"));
    }

    @Test
    void getUser() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        uService.register(user);

        var grabbedUser = uService.getUser(user.username());

        assertEquals(user, grabbedUser);
    }

    @Test
    void getUserFail() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        uService.register(user);

        var grabbedUser = uService.getUser(user.username() + "badStuff");

        assertNotEquals(user, grabbedUser);
    }

    @Test
    void getAuth() throws DataAccessException {
        var auth = addUser();
        var grabbedAuth = aService.getAuth(auth.authToken());

        assertEquals(auth, grabbedAuth);
    }

    @Test
    void getAuthFail() throws DataAccessException {
        var auth = addUser();
        var grabbedAuth = aService.getAuth(auth.authToken() + "badStuff");

        assertNotEquals(auth, grabbedAuth);
    }

    AuthData addUser() throws DataAccessException{
        var user = new UserData("john doe", "password", "example@mail.com");
        return uService.register(user);
    }
}
