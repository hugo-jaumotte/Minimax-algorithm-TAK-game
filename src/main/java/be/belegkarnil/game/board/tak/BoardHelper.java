package be.belegkarnil.game.board.tak;

import java.awt.Color;
import java.awt.Point;

// Subclass of Board that allows the use of private methods
public class BoardHelper extends Board {

    public BoardHelper(Board board) {
        super(board);
    }

    public Color applyPlace(Piece piece, Point position) {
        return super.place(piece, position);
    }

    public Color applyMove(Point src, int[] amount, Point dst) {
        return super.move(src, amount, dst);
    }

    public static BoardHelper copyOf(Board board) {
        return new BoardHelper(board);
    }
}
