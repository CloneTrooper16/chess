package client;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ServerFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String userName = null;
    private AuthData userAuth = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private final Map<Integer, GameData> games = new HashMap<>();

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
            this.games.put(i, game);
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
                int id = Integer.parseInt(params[0]);
                var gameInfo = games.get(id);
                int gameID = gameInfo.gameID();
                String board = gameInfo.game().getBoard().toString();
                if (!(params[1].equals("white") || params[1].equals("black"))) {
                    throw new ResponseException(400, "Invalid player color");
                } else if (params[1].equals("white")) {
                    server.joinGame(userAuth.authToken(), ChessGame.TeamColor.WHITE, gameID);
                } else {
                    server.joinGame(userAuth.authToken(), ChessGame.TeamColor.BLACK, gameID);
                }

                return printWhiteBlackBoards(board);
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
            if (games.isEmpty()) {
                return "Please list games first";
            }
            else if (isValidGameID(params[0])) {
                int id = Integer.parseInt(params[0]);
                var gameInfo = games.get(id);
                int gameID = gameInfo.gameID();
                String board = gameInfo.game().getBoard().toString();
                return printWhiteBlackBoards(board);
            }
            else {
                throw new ResponseException(400, "Invalid game id");
            }
        }
        throw new ResponseException(400, "Expected: <id>");
    }

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

    private String printWhiteBlackBoards(String blackBoard) {
        String whiteBoard = reverseBoard(blackBoard);
        whiteBoard = addBoardLetters(whiteBoard);
        blackBoard = rotateBoard(whiteBoard);
        return printboard(blackBoard) + "\n" + printboard(whiteBoard);
    }

    private String printboard(String board) {
        String lightSquareColor = SET_BG_COLOR_WHITE;
        String darkSquareColor = SET_BG_COLOR_DARK_GREY;
        String lightPieceColor = SET_TEXT_COLOR_AQUA;
        String darkPieceColor = SET_TEXT_COLOR_RED;
        boolean lastFirstSquareLight = false;
        boolean edgeSquare = true;
        SquareColor currentSquareColor = SquareColor.LIGHT;
        var result = new StringBuilder();
        result.append(ERASE_SCREEN);
        for (int i = 0; i < board.length(); i++) {
            char c = board.charAt(i);
            if (edgeSquare) {
                result.append(SET_BG_COLOR_LIGHT_GREY);
            } else if (currentSquareColor == SquareColor.LIGHT) {
                result.append(lightSquareColor);
            } else {
                result.append(darkSquareColor);
            }
            if (Character.isLowerCase(c)) {
                result.append(darkPieceColor);
            } else {
                result.append(lightPieceColor);
            }
            if (c == '\n') {
                result.append(RESET_BG_COLOR);
                result.append(c);
            }
            else if (c == '_') {
                result.append(EMPTY);
            } else {
                String s = String.valueOf(c);
                s = findChessUnicode(s, board, i);
                result.append(s);
            }
            if (i < board.length() - 1) {
                if (nextSquareOnBoard(c, board.charAt(i+1)) && lastFirstSquareLight) {
                    currentSquareColor = SquareColor.LIGHT;
                    lastFirstSquareLight = false;
                } else if (nextSquareOnBoard(c, board.charAt(i+1)) && !lastFirstSquareLight) {
                    currentSquareColor = SquareColor.DARK;
                    lastFirstSquareLight = true;
                }
                if (edgeSquare && nextSquareOnBoard(c, board.charAt(i+1))) {
                    edgeSquare = false;
                } else if (nextSquareOnEdge(board.charAt(i+1))) {
                    edgeSquare = true;
                }
            }
            if (!edgeSquare && currentSquareColor == SquareColor.LIGHT) {
                currentSquareColor = SquareColor.DARK;
            } else {
                currentSquareColor = SquareColor.LIGHT;
            }

        }
        result.append(RESET_BG_COLOR);
        return result.toString();
    }

    private enum SquareColor{
        LIGHT,
        DARK,
    }

    private String rotateBoard(String board) {
        String[] lines = board.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new StringBuilder(lines[i]).reverse().toString();
        }
        Collections.reverse(Arrays.asList(lines));
        return String.join("\n", lines);
    }

    private String reverseBoard(String board) {
        String[] lines = board.split("\n");
        Collections.reverse(Arrays.asList(lines));
        return String.join("\n", lines);
    }

    private String addBoardLetters(String board) {
        String[] lines = board.split("\n");
        var result = new StringBuilder();
        int i = 8;
        result.append("_");
        result.append(fullWidthCharacter("abcdefgh"));
        result.append("_\n");
        for (String line: lines) {
            result.append(fullWidthCharacter(String.valueOf(i)));
            result.append(line);
            result.append(i);
            result.append("\n");
            i--;
        }
        result.append("_");
        result.append(fullWidthCharacter("abcdefgh"));
        result.append("_\n");
        return result.toString();
    }
    private boolean nextSquareOnBoard(char curr, char next) {
        if (Character.isDigit(curr) && next != '\n') {
            return true;
        }
        return false;
    }

    private boolean nextSquareOnEdge(char next) {
        return Character.isDigit(next);
    }

    private String fullWidthCharacter(String characters) {
        var result = new StringBuilder();
        for (char c : characters.toCharArray()) {
            result.append((char) (c + 0xFEE0));
        }
        return result.toString();
    }

    private String findChessUnicode(String s, String board, int i) {
        switch(s) {
            case "K" -> s = WHITE_KING;
            case "Q" -> s = WHITE_QUEEN;
            case "B" -> s = WHITE_BISHOP;
            case "N" -> s = WHITE_KNIGHT;
            case "P" -> s = WHITE_PAWN;
            case "R" -> s = WHITE_ROOK;
            case "k" -> s = BLACK_KING;
            case "q" -> s = BLACK_QUEEN;
            case "b" -> {
                if ((board.charAt(i + 1) != 'c' && board.charAt(i + 1) != 'a')) {
                    s = BLACK_BISHOP;
                }
            }
            case "n" -> s = BLACK_KNIGHT;
            case "p" -> s = BLACK_PAWN;
            case "r" -> s = BLACK_ROOK;
            default -> s = s;
        }
        return s;
    }

}
