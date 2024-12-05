package handler;

import chess.*;
import chess.rulebook.FIDERuleBook;
import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.GameMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;
import websocket.ConnectionManager;
import websocket.commands.HighlightMovesCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static ui.EscapeSequences.*;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthService authService;
    private final GameService gameService;

    public WebSocketHandler() throws ServerException {
        AuthDAO authDataAccess = new DatabaseAuthDAO();
        GameDAO gameDataAccess = new DatabaseGameDAO();
        this.authService = new AuthService(authDataAccess);
        this.gameService = new GameService(gameDataAccess, authDataAccess);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, ServerException {
        int ctIndex = message.indexOf("commandType");
        char commandType = message.charAt(ctIndex + 14);
        if (!(commandType == 'M' || commandType == 'H')) {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getGameID(), command.getAuthToken(), session);
                case LEAVE -> leave(command.getGameID(), command.getAuthToken(), session);
                case RESIGN -> resign(command.getGameID(), command.getAuthToken(), session);
                case DRAW -> redraw(command.getGameID(), command.getAuthToken(), session);
            }
        } else {
            if (commandType == 'M') {
                MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
                makeMove(command.getGameID(), command.getAuthToken(), command.getMove(), session);
            } else {
                HighlightMovesCommand command = new Gson().fromJson(message, HighlightMovesCommand.class);
                highlightMoves(command.getGameID(), command.getAuthToken(), command.getPos(), session);
            }
        }

        System.out.printf("Received: %s\n", message);
    }


    private void handleError(Session session, String errorString) throws IOException {
        System.out.printf("Error: %s\n", errorString);
        var errorMessage = new ErrorMessage(errorString);
        session.getRemote().sendString(new Gson().toJson(errorMessage));
    }

    private void connect(int gameID, String authToken, Session session) throws IOException, ServerException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (game == null) {
            handleError(session, "error: invalid gameID");
            return;
        }
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        String username = auth.username();
        connections.add(gameID, username, session);
        String color = getPlayerColor(game, username);
        ChessGame.TeamColor teamColor = getTeamColor(color);
        var message = String.format("%s has joined the game as %s", username, color);
        var notification = new Notification(message);
        connections.broadcast(session, username, notification);
        String boardString = createBoardString(game, teamColor, null);
        var gameMessage = new GameMessage(gameID, teamColor, boardString);
        var loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.send(session, loadGame);
    }

    private void leave(int gameID, String authToken, Session session) throws ServerException, IOException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        if (game == null) {
            handleError(session, "error: invalid gameID");
            return;
        }
        String username = auth.username();
        var message = String.format("%s has left the game", username);
        var notification = new Notification(message);
        connections.broadcast(session, username, notification);
        connections.remove(session);
        if (userWasPlayer(game, username)) {
            var newGame = removePlayer(game, username);
            gameService.updateGame(authToken, newGame);
        }
    }

    private void resign(int gameID, String authToken, Session session) throws IOException, ServerException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        String username = auth.username();
        if (!userWasPlayer(game, username)) {
            handleError(session, "error: observers can't resign");
            return;
        }
        if (game.isOver()) {
            handleError(session, "error: can't resign, game is already finished");
            return;
        }
        var newGame = endGame(game);
        gameService.updateGame(authToken, newGame);
        var message = String.format("%s has resigned", username);
        var notification = new Notification(message);
        connections.broadcast(session, null, notification);
    }

    private void redraw(int gameID, String authToken, Session session) throws ServerException, IOException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (game == null) {
            handleError(session, "error: invalid gameID");
            return;
        }
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        String username = auth.username();
        String color = getPlayerColor(game, username);
        ChessGame.TeamColor teamColor = getTeamColor(color);
        String boardString = createBoardString(game, teamColor, null);
        var gameMessage = new GameMessage(gameID, teamColor, boardString);
        var loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.send(session, loadGame);
    }

    private void highlightMoves(int gameID, String authToken, ChessPosition pos, Session session) throws ServerException, IOException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (game == null) {
            handleError(session, "error: invalid gameID");
            return;
        }
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        String username = auth.username();
        String color = getPlayerColor(game, username);
        ChessGame.TeamColor teamColor = getTeamColor(color);
        String boardString = createBoardString(game, teamColor, pos);
        var gameMessage = new GameMessage(gameID, teamColor, boardString);
        var loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.send(session, loadGame);
    }

    private void makeMove(int gameID, String authToken, ChessMove move, Session session) throws ServerException, IOException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        if (auth == null) {
            handleError(session, "error: invalid auth token");
            return;
        }
        String username = auth.username();
        if (!userWasPlayer(game, username)) {
            handleError(session, "error: observers can't move pieces");
            return;
        }
        if (game.isOver()) {
            handleError(session, "error: can't move piece, game is finished");
            return;
        }
        String color = getPlayerColor(game, username);
        ChessGame.TeamColor teamColor = getTeamColor(color);
        if (!isTurn(game.game(), teamColor)) {
            handleError(session, "error: can't move piece; it's not your turn");
            return;
        }
        ChessBoard board = game.game().getBoard();
        try {
            GameData newGame = doTheMove(game, board, move);
            if (isMate(game.game())) {
                newGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                        game.gameName(), game.game(), true);
            }
            String bothBoardString = createBoardString(newGame, ChessGame.TeamColor.WHITE, null)
                                    + "SPLITTER"
                                    + createBoardString(newGame, ChessGame.TeamColor.BLACK, null);
            var gameMessage = new GameMessage(gameID, teamColor, bothBoardString);
            var loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
            connections.broadcastNewBoard(session, loadGame);
            gameService.updateGame(authToken, newGame);

            var message = String.format("%s moved a piece from %s to %s", username,
                    parsePos(move.getStartPosition().toString()), parsePos(move.getEndPosition().toString()));
            var notification = new Notification(message);
            connections.broadcast(session, username, notification);
        } catch (InvalidMoveException ex) {
            handleError(session, "error: invalid move");
        }

    }
    //hopefully this works!
    private String parsePos(String pos) {
        int rowIndex = pos.indexOf("row");
        int colIndex = pos.indexOf("col");
        char rowChar = pos.charAt(rowIndex + 4);
        char colChar = pos.charAt(colIndex + 4);

        return parseCol(colChar) + parseRow(rowChar);
    }

    private String parseCol(char colChar) {
        switch(colChar) {
            case '1' -> {
                return "a";
            }
            case '2' -> {
                return "b";
            }
            case '3' -> {
                return "c";
            }
            case '4' -> {
                return "d";
            }
            case '5' -> {
                return "e";
            }
            case '6' -> {
                return "f";
            }
            case '7' -> {
                return "g";
            }
            case '8' -> {
                return "h";
            }
        }
        return "0";
    }

    private String parseRow(char rowInt) {
        return String.valueOf(rowInt);
    }

    private GameData doTheMove(GameData game, ChessBoard board, ChessMove move) throws InvalidMoveException {
        game.game().makeMove(move);
        return game;
    }

    private boolean isTurn(ChessGame game, ChessGame.TeamColor color) {
        return color == game.getTeamTurn();
    }

    private boolean isMate(ChessGame game) {
        return game.isGameOver();
    }

    private boolean userWasPlayer(GameData game, String username) {
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            return true;
        } else return game.blackUsername() != null && game.blackUsername().equals(username);
    }

    private GameData removePlayer(GameData game, String userToRemove) {
        if (game.whiteUsername().equals(userToRemove)) {
            return new GameData(game.gameID(), null, game.blackUsername(),
                    game.gameName(), game.game(), game.isOver());
        }
        return new GameData(game.gameID(), game.whiteUsername(), null,
                game.gameName(), game.game(), game.isOver());
    }

    private GameData endGame(GameData game) {
        return new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(),
                game.gameName(), game.game(), true);
    }

    private String getPlayerColor(GameData game, String username) {
        if (game.whiteUsername() != null && game.whiteUsername().equals(username)) {
            return "white";
        } else if (game.blackUsername() != null && game.blackUsername().equals(username)) {
            return "black";
        }
        return "an observer";
    }

    private ChessGame.TeamColor getTeamColor(String color) {
        if (color.equals("black")) {
            return ChessGame.TeamColor.BLACK;
        }
        return ChessGame.TeamColor.WHITE;
    }

    private String createBoardString(GameData gameData, ChessGame.TeamColor color, ChessPosition pos) throws ServerException {
        int gameId = gameData.gameID();
        String board = gameData.game().getBoard().toString();
        ChessBoard chessBoard = gameData.game().getBoard();
        String whiteBoard = reverseBoard(board);
        whiteBoard = addBoardLetters(whiteBoard);
        HashSet<ChessPosition> moves = new HashSet<>();
        if (pos != null) {
            moves.addAll(getLegalMoves(pos, chessBoard));
        }
        if (color == ChessGame.TeamColor.BLACK) {
            return printBoard(rotateBoard(whiteBoard), moves, false);
        } else {
            return printBoard(whiteBoard, moves, true);
        }
    }

    private Collection<ChessPosition> getLegalMoves(ChessPosition pos, ChessBoard board) {
        var ruleBook = new FIDERuleBook(board);
        var validMoves = ruleBook.validMoves(pos);
        HashSet<ChessPosition> options = new HashSet<>();
        for (ChessMove move : validMoves) {
            options.add(move.getEndPosition());
        }
        return options;
    }


    private String printWhiteBlackBoards(String blackBoard) {
        String whiteBoard = reverseBoard(blackBoard);
        whiteBoard = addBoardLetters(whiteBoard);
        blackBoard = rotateBoard(whiteBoard);
//        return printBoard(blackBoard) + "\n\n" + printBoard(whiteBoard);
        return "";
    }

    private String printBoard(String board, Collection<ChessPosition> moves, boolean isWhite) {
        String lightSquareColor = SET_BG_COLOR_WHITE;
        String darkSquareColor = SET_BG_COLOR_DARK_GREY;
        String lightPieceColor = SET_TEXT_COLOR_AQUA;
        String darkPieceColor = SET_TEXT_COLOR_RED;
        String highLightColor = SET_BG_COLOR_GREEN;
        String highDarkColor = SET_BG_COLOR_DARK_GREEN;
        int row = isWhite ? 9 : 0;
        int col = isWhite ? 0 : 9;
        boolean lastFirstSquareLight = false;
        boolean edgeSquare = true;
        SquareColor currentSquareColor = SquareColor.LIGHT;
        var result = new StringBuilder();
        result.append(ERASE_SCREEN);
        for (int i = 0; i < board.length(); i++) {
            char c = board.charAt(i);
            if (edgeSquare || c == ' ') {
                result.append(SET_BG_COLOR_LIGHT_GREY);
            } else if (currentSquareColor == SquareColor.LIGHT) {
                col += isWhite ? 1 : -1;
                if (isAMove(row, col, moves)) {
                    result.append(highLightColor);
                } else {
                    result.append(lightSquareColor);
                }
            } else {
                col += isWhite ? 1 : -1;
                if (isAMove(row, col, moves)) {
                    result.append(highDarkColor);
                } else {
                    result.append(darkSquareColor);
                }
            }
            if (edgeSquare) {
                result.append(SET_TEXT_COLOR_PURPLE);
            } else if (Character.isLowerCase(c)) {
                result.append(darkPieceColor);
            } else {
                result.append(lightPieceColor);
            }
            if (c == '\n') {
                result.append(RESET_BG_COLOR);
                result.append(c);
                row += isWhite ? -1 : 1;
                col = isWhite ? 0 : 9;
            }
            else if (c == '_') {
                result.append(EMPTY);
            } else {
                String s = String.valueOf(c);
                s = findChessUnicode(s, board, i);
                result.append(s);
            }
            if (i < board.length() - 1) {
                var nextChar = findNextChar(c, board, i);
                if (nextSquareOnBoard(c, nextChar) && lastFirstSquareLight) {
                    currentSquareColor = SquareColor.DARK;
                    lastFirstSquareLight = false;
                } else if (nextSquareOnBoard(c, nextChar) && !lastFirstSquareLight) {
                    currentSquareColor = SquareColor.LIGHT;
                    lastFirstSquareLight = true;
                }
                if (edgeSquare && nextSquareOnBoard(c, nextChar)) {
                    edgeSquare = false;
                } else if (nextSquareOnEdge(nextChar)) {
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
            result.append(fullWidthCharacter(String.valueOf(i)));
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
            result.append(" ");
            result.append(c);
            result.append(" ");
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
                char nextChar = findNextChar(s.charAt(0), board, i);
                if ((nextChar != 'c' && nextChar != 'a')) {
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

    private char findNextChar(char c, String board, int i) {
        int j = i + 1;
        char res = 'i';
        while (res == 'i') {
            if (board.charAt(j) == ' ') {
                j++;
            } else {
                res = board.charAt(j);
            }
        }
        return res;
    }

    private boolean isAMove(int row, int col, Collection<ChessPosition> moves) {
        for (ChessPosition move : moves) {
            if (move.equals(new ChessPosition(row, col))) {
                return true;
            }
        }
        return false;
    }
}
