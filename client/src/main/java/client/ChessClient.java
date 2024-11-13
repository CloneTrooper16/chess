package client;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String userName = null;
    private AuthData userAuth = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private final Map<Integer, Integer> games = new HashMap<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            UserData newUser = new UserData(params[0], params[1], params[2]);
            userAuth = server.register(newUser);
            state = State.LOGGED_IN;
            userName = params[0];
            return String.format("You signed in as %s.", userName);
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            UserData newUser = new UserData(params[0], params[1], null);
            userAuth = server.login(newUser);
            state = State.LOGGED_IN;
            userName = params[0];
            return String.format("You signed in as %s.", userName);
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout() throws ResponseException {
        assertLoggedIn();
        server.logout(userAuth.authToken());
        state = State.LOGGED_OUT;
        return String.format("%s logged out. Goodbye!", userName);
    }

    public String createGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length == 1) {
            int gameID = server.createGame(userAuth.authToken(), params[0]);
            return String.format("Created game: %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <name>");
    }

    public String listGames() throws ResponseException {
        assertLoggedIn();
        var games = server.listGames(userAuth.authToken());
        var result = new StringBuilder();
        int i = 1;
        this.games.clear();
        for (var game : games.games()) {
            result.append(i);
            result.append(". ");
            result.append(game.gameName());
            if (game.gameName().length() < 5) {
                result.append("\t");
            }
            result.append("\t\twhite: ");
            result.append(game.whiteUsername() == null ? "no player" : game.whiteUsername());
            if (game.whiteUsername() != null && game.whiteUsername().length() < 9) {
                result.append("\t");
            }
            if (game.whiteUsername() != null && game.whiteUsername().length() < 5) {
                result.append("\t");
            }
            result.append("\tblack: ");
            result.append(game.blackUsername() == null ? "no player" : game.blackUsername());
            result.append("\n");
            this.games.put(i, game.gameID());
            i++;
        }
        return result.toString();
    }

    public String joinGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length == 2) {
            if (games.isEmpty()) {
                return "Please list games first";
            }
            else if (isValidGameID(params[0])) {
                if (!(params[1].equals("white") || params[1].equals("black"))) {
                    throw new ResponseException(400, "Invalid player color");
                } else if (params[1].equals("white")) {
                    server.joinGame(userAuth.authToken(), ChessGame.TeamColor.WHITE, Integer.parseInt(params[0]));
                } else {
                    server.joinGame(userAuth.authToken(), ChessGame.TeamColor.BLACK, Integer.parseInt(params[0]));
                }
                return String.format("joined game: %s", params[0]);
            }
            else {
                throw new ResponseException(400, "Invalid game id");
            }
        }
        throw new ResponseException(400, "Expected: <id> [WHITE|BLACK]");
    }

    public String observeGame(String... params) throws ResponseException {
        assertLoggedIn();
        if (params.length == 1) {
//            int gameID = server.createGame(userAuth.authToken(), params[0]);
            if (games.isEmpty()) {
                return "Please list games first";
            }
            else if (isValidGameID(params[0])) {
                return String.format("Observing game: %s (Not implemented)", params[0]);
            }
            else {
                throw new ResponseException(400, "Invalid game id");
            }
        }
        throw new ResponseException(400, "Expected: <id>");
    }

//    public String rescuePet(String... params) throws ResponseException {
//        assertLoggedIn();
//        if (params.length >= 2) {
//            var name = params[0];
//            var type = PetType.valueOf(params[1].toUpperCase());
//            var pet = new Pet(0, name, type);
//            pet = server.addPet(pet);
//            return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
//        }
//        throw new ResponseException(400, "Expected: <name> <CAT|DOG|FROG>");
//    }
//    public String adoptAllPets() throws ResponseException {
//        assertLoggedIn();
//        var buffer = new StringBuilder();
//        for (var pet : server.listPets()) {
//            buffer.append(String.format("%s says %s%n", pet.name(), pet.sound()));
//        }
//
//        server.deleteAllPets();
//        return buffer.toString();


    public String help() {
        String primaryColor = SET_TEXT_COLOR_BLUE;
        String secondaryColor = SET_TEXT_COLOR_MAGENTA;
        if (state == State.LOGGED_OUT) {
            return primaryColor + " - register <username> <password> <email>"
                    + secondaryColor + " - to create an account \n"
                    + primaryColor + " - login <username> <password>"
                    + secondaryColor + " - to play chess \n"
                    + primaryColor + " - quit"
                    + secondaryColor + " - exits chess program\n"
                    + primaryColor + " - help"
                    + secondaryColor + " - displays this menu \n";
        }
        return primaryColor + " - create <name>"
                + secondaryColor + " - creates a game \n"
                + primaryColor + " - list"
                + secondaryColor + " - lists all games \n"
                + primaryColor + " - join <id> [WHITE|BLACK]"
                + secondaryColor + " - joins a game\n"
                + primaryColor + " - observe <id>"
                + secondaryColor + " - observe an ongoing game\n"
                + primaryColor + " - logout"
                + secondaryColor + " - leaves the chess program\n"
                + primaryColor + " - quit"
                + secondaryColor + " - exits chess program\n"
                + primaryColor + " - help"
                + secondaryColor + " - displays this menu \n";
    }

    public State getState() {
        return state;
    }

    private void assertLoggedIn() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }

    private boolean isValidGameID(String id) {
        try {
            int gameID = Integer.parseInt(id);
            for (int gameNum : games.keySet()) {
                if (gameNum == gameID) {
                    return true;
                }
            }
        } catch(Exception e) {
            return false;
        }
        return false;
    }
}
