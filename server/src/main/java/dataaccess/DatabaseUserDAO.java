package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DatabaseUserDAO implements UserDAO {
    public DatabaseUserDAO() throws ServerException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public UserData addUser(UserData u) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        var id = executeUpdate(statement, u.username(), u.password(), u.email()); //TODO: bcrypt this.
        return new UserData(u.username(), u.password(), u.email());
    }
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (var chess = conn.prepareStatement(statement)) {
                chess.setString(1, username);
                try (var rs = chess.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAllUsers() throws DataAccessException {

    }

    private UserData readUser(ResultSet rs) throws SQLException {
//        var id = rs.getInt("id"); probably don't need this
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS users (
          `id` int NOT NULL AUTO_INCREMENT,
          `username` varchar(256) NOT NULL,
          `password` varchar(256) NOT NULL,
          `email` varchar(256) NOT NULL,
          PRIMARY KEY (`id`),
          INDEX(username)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var chess = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    switch (param) {
                        case String p -> chess.setString(i + 1, p);
                        case Integer p -> chess.setInt(i + 1, p);
                        case null -> chess.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                chess.executeUpdate();

                var rs = chess.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
