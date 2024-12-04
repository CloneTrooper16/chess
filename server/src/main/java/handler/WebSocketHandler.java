package handler;

import chess.ChessGame;
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
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

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
        if (commandType != 'M') {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getGameID(), command.getAuthToken(), session);
                case MAKE_MOVE -> exit(command.getCommandType());
            }
        } else {
            MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
        }

        System.out.printf("Received: %s\n", message);
    }

    private void connect(int gameID, String authToken, Session session) throws IOException, ServerException {
        AuthData auth = authService.getAuth(authToken);
        GameData game = gameService.getGame(gameID);
        String username = auth.username();
        String color = getPlayerColor(game, username);
        ChessGame.TeamColor teamColor = getTeamColor(color);
        connections.add(gameID, username, session);
        var message = String.format("%s has joined the game as %s", username, color);
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, username, notification);
        String boardString = createBoardString(gameID, teamColor);
        var gameMessage = new GameMessage(gameID, teamColor, boardString);
        var loadGame = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameMessage);
        connections.send(session, loadGame);
    }

    private void exit(UserGameCommand.CommandType visitorName) throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var notification = new Notification(Notification.Type.DEPARTURE, message);
//        connections.broadcast(visitorName, notification);
    }

//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast("", notification);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }

    private String getPlayerColor(GameData game, String username) {
        if (game.whiteUsername().equals(username)) {
            return "white";
        }
        return "black";
    }

    private ChessGame.TeamColor getTeamColor(String color) {
        if (color.equals("white")) {
            return ChessGame.TeamColor.WHITE;
        }
        return ChessGame.TeamColor.BLACK;
    }

    private String createBoardString(int gameID, ChessGame.TeamColor color) throws ServerException {
        GameData gameData = gameService.getGame(gameID);
        int gameId = gameData.gameID();
        String board = gameData.game().getBoard().toString();
        String whiteBoard = reverseBoard(board);
        whiteBoard = addBoardLetters(whiteBoard);
        if (color == ChessGame.TeamColor.WHITE) {
            return printBoard(whiteBoard);
        } else {
            return printBoard(rotateBoard(whiteBoard));
        }
    }


    private String printWhiteBlackBoards(String blackBoard) {
        String whiteBoard = reverseBoard(blackBoard);
        whiteBoard = addBoardLetters(whiteBoard);
        blackBoard = rotateBoard(whiteBoard);
        return printBoard(blackBoard) + "\n\n" + printBoard(whiteBoard);
    }

    private String printBoard(String board) {
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
            if (edgeSquare || c == ' ') {
                result.append(SET_BG_COLOR_LIGHT_GREY);
            } else if (currentSquareColor == SquareColor.LIGHT) {
                result.append(lightSquareColor);
            } else {
                result.append(darkSquareColor);
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
}
