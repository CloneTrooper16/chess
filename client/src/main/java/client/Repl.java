package client;

import client.websocket.NotificationHandler;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this);
    }



    public void run() {
        System.out.println("Welcome to Chess. Type help to get started.");
//        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    public void notify(String message) {
        System.out.println("\n" + RESET_TEXT_COLOR + SET_TEXT_COLOR_RED + message);
        printPrompt();
    }

    public void printBoard(String board) {
        System.out.println("\n" + board);
        printPrompt();
    }

    public void notifyError(String message) {
        System.out.println("\n" + RESET_TEXT_COLOR + SET_TEXT_COLOR_RED + message);
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[" + client.getState() + "] >>> " + SET_TEXT_COLOR_GREEN);
    }
}
