package chess;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row, col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) obj;
        return that.row == row && that.col == col;
    }

    @Override
    public int hashCode() {
        return 31 * row + 37 * col;
    }

    @Override
    public String toString() {
        return "(" +
                "row-" + row +
                ", col-" + col +
                ")";
    }

    public static ChessPosition fromString(String positionString) {
        try {
            positionString = positionString.replaceAll("[()]", "").trim();
            String[] parts = positionString.split(", ");
            int row = Integer.parseInt(parts[0].split("-")[1]); // Get the second part after "row-"
            int col = Integer.parseInt(parts[1].split("-")[1]); // Get the second part after "col-"
            return new ChessPosition(row, col);
        } catch(Error e) {
            throw new IllegalArgumentException("Invalid format: " + positionString);
        }
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }
}
