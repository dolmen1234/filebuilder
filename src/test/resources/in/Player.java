import java.util.*;

class Player {

    static Cell [][] cells = new Cell[8][8];
    static Cell rook;
    static List<String> moves = new ArrayList<>();
    static final Cell [] incs = new Cell [] { new Cell(0, 1), new Cell(0, -1), new Cell(1, 0), new Cell(-1,0)};

    public static void main(String args[]) {

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                cells[row][col] = new Cell(row, col);
            }
        }

        Scanner in = new Scanner(System.in);
        String rookPosition = in.nextLine();
        rook = new Cell("0 "+rookPosition);

        int nbPieces = Integer.parseInt(in.nextLine());
        for (int i = 0; i < nbPieces; i++) {
            Cell piece = new Cell(in.nextLine());
            cells[piece.row][piece.col] = piece;
        }

        for (Cell inc : incs){
            addMoves(inc);
        }

        Collections.sort(moves);
        for (String move : moves) {
            System.out.println(move);
        }        
    }
    
    
    static void addMoves(Cell inc) {
        Cell current = new Cell(rook);
        while (true) {
            current.inc(inc);
            if (current.isInvalid()) {
                return;
            }
            current.state = cells[current.row][current.col].state;
            if (current.isWhite()) {
                return;
            }
            if (current.isEmpty()) {
                moves.add("R"+rook.toString()+"-"+current.toString());
            } else {
                moves.add("R"+rook.toString()+"x"+current.toString());
                return;
            }
        }
    }
}

