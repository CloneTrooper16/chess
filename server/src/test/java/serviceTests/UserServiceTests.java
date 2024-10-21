package serviceTests;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static service.AuthService.generateToken;

public class UserServiceTests {
    private static final AuthDAO authDataAccess = new MemoryAuthDAO();
    static final UserService uService = new UserService(new MemoryUserDAO(), authDataAccess);
    static final AuthService aService = new AuthService(authDataAccess);

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
        var auth = uService.register(user);

        assertThrows(DataAccessException.class, () -> uService.register(user));
    }

    @Test
    void logout() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = uService.register(user);

        assertDoesNotThrow(() -> aService.logout(auth.authToken()));
    }

    @Test
    void logoutFail() throws DataAccessException {
        var user = new UserData("john doe", "password", "example@mail.com");
        var auth = uService.register(user);

//        uService.logout(auth);

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
}
