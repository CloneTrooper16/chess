import chess.*;
import client.Repl;
import exception.ResponseException;

public class Main {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        try {
            new Repl(serverUrl).run();
        } catch (ResponseException e) {
            throw new RuntimeException(e);
        }
    }
}