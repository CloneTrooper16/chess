package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import server.Server;

import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class DatabaseAuthDAO implements AuthDAO {
    public DatabaseAuthDAO() throws ServerException {
        DatabaseManager.configureDatabase(createStatements);
    }

    public AuthData addAuth(AuthData a) throws DataAccessException {
        var statement = "INSERT INTO auths (username, authToken) VALUES (?, ?)";
        var id = executeUpdate(statement, a.username(), a.authToken());
        return new AuthData(a.authToken(), a.username());
    }
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auths WHERE authToken=?";
            try (var chess = conn.prepareStatement(statement)) {
                chess.setString(1, authToken);
                try (var rs = chess.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public AuthData getAuthByUsername(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auths WHERE username=?";
            try (var chess = conn.prepareStatement(statement)) {
                chess.setString(1, username);
                try (var rs = chess.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        executeUpdate(statement, authData.authToken());
    }

    public void deleteAllAuths() throws DataAccessException {

    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var authToken = rs.getString("authToken");
        return new AuthData(authToken, username);
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS auths (
          `id` int NOT NULL AUTO_INCREMENT,
          `username` varchar(256) UNIQUE NOT NULL,
          `authToken` varchar(256) NOT NULL,
          PRIMARY KEY (`id`),
          INDEX(authToken),
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
