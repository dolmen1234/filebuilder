import java.util.*;

class Cell {
    static final int EMPTY = 2;
    static final int BLACK = 1;
    static final int WHITE = 0;

    int state = EMPTY;
    int row;
    int col;

    Cell(Cell other) {
        this(other.row, other.col);
        state = other.state;
    }
    
    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    Cell(String in) {
        String [] tokens = in.split(" ");
        state = Integer.parseInt(tokens[0]);
        row = tokens[1].charAt(1)-'1';
        col = tokens[1].charAt(0)-'a';
    }
    
    void inc(Cell other) {
        row += other.row;
        col += other.col;
    }
    
    boolean isEmpty() {
        return EMPTY ==state;
    }
    
    boolean isWhite() {
        return state == WHITE;
    }

    boolean isInvalid() {
        return row < 0 || row > 7 || col < 0 || col > 7;
    }
    
    public String toString() {
        return String.valueOf((char)('a'+col)) + String.valueOf(1+row);
    }
    
}


