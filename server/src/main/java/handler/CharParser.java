package handler;

public class CharParser {
    public String parseCol(char colChar) {
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

    public String parseRow(char rowInt) {
        return String.valueOf(rowInt);
    }
}
