package serviceTests;

import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import service.UserService;

public class UserServiceTests {
    static final UserService uService = new UserService(new MemoryUserDAO(), new MemoryAuthDAO());
}
