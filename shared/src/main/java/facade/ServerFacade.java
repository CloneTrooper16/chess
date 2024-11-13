package facade;

import chess.ChessGame;
import com.google.gson.Gson;
import deserializer.ChessDeserializer;
import exception.ResponseException;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, null, userData, AuthData.class);
    }

    public AuthData login(UserData userData) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path, null, userData, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", authToken);
        this.makeRequest("DELETE", path, headers, null, null);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", authToken);
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
        var gameResponse = this.makeRequest("POST", path, headers, createGameRequest, CreateGameResponse.class);
        return gameResponse.gameID();
    }

    public ListGamesResponse listGames(String authToken) throws ResponseException {
        var path = "/game";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", authToken);
        return this.makeRequest("GET", path, headers, null, ListGamesResponse.class);
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws ResponseException {
        var path = "/game";
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", authToken);
        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, gameID);
        this.makeRequest("PUT", path, headers, joinGameRequest, null);
    }

    public void clear() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    private <T> T makeRequest(String method, String path, Map<String, String> headers,
                              Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    http.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            String msg = "failure: " + status;
            if (status == 401) {
                msg = "Invalid username or password";
            } else if (status == 403) {
                msg = "Already taken";
            }
            throw new ResponseException(status, msg);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    Gson gson = new ChessDeserializer().createChessGson();
                    response = gson.fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
