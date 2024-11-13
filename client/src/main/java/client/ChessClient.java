package client;

import exception.ResponseException;
import model.AuthData;
import model.UserData;
import server.ServerFacade;

import java.util.Arrays;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String userName = null;
    private AuthData userAuth = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;

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
//                case "list" -> listPets();
//                case "signout" -> signOut();
//                case "adopt" -> adoptPet(params);
//                case "adoptall" -> adoptAllPets();
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
        assertSignedIn();
        server.logout(userAuth.authToken());
        state = State.LOGGED_OUT;
        return String.format("%s logged out. Goodbye!", userName);
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            int gameID = server.createGame(userAuth.authToken(), params[0]);
            return String.format("Created game: %s", params[0]);
        }
        throw new ResponseException(400, "Expected: <name>");
    }

//    public String rescuePet(String... params) throws ResponseException {
//        assertSignedIn();
//        if (params.length >= 2) {
//            var name = params[0];
//            var type = PetType.valueOf(params[1].toUpperCase());
//            var pet = new Pet(0, name, type);
//            pet = server.addPet(pet);
//            return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
//        }
//        throw new ResponseException(400, "Expected: <name> <CAT|DOG|FROG>");
//    }

//    public String listPets() throws ResponseException {
//        assertSignedIn();
//        var pets = server.listPets();
//        var result = new StringBuilder();
//        var gson = new Gson();
//        for (var pet : pets) {
//            result.append(gson.toJson(pet)).append('\n');
//        }
//        return result.toString();
//    }

//    public String adoptPet(String... params) throws ResponseException {
//        assertSignedIn();
//        if (params.length == 1) {
//            try {
//                var id = Integer.parseInt(params[0]);
//                var pet = getPet(id);
//                if (pet != null) {
//                    server.deletePet(id);
//                    return String.format("%s says %s", pet.name(), pet.sound());
//                }
//            } catch (NumberFormatException ignored) {
//            }
//        }
//        throw new ResponseException(400, "Expected: <pet id>");
//    }

//    public String adoptAllPets() throws ResponseException {
//        assertSignedIn();
//        var buffer = new StringBuilder();
//        for (var pet : server.listPets()) {
//            buffer.append(String.format("%s says %s%n", pet.name(), pet.sound()));
//        }
//
//        server.deleteAllPets();
//        return buffer.toString();
//    }

//    public String signOut() throws ResponseException {
//        assertSignedIn();
//        ws.leavePetShop(visitorName);
//        ws = null;
//        state = State.LOGGED_OUT;
//        return String.format("%s left the shop", visitorName);
//    }

//    private Pet getPet(int id) throws ResponseException {
//        for (var pet : server.listPets()) {
//            if (pet.id() == id) {
//                return pet;
//            }
//        }
//        return null;
//    }

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

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}
