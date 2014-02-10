package deciduous;

import static deciduous.Color.*;

/** A simple container class holding details about a move.
 *  @author Andrew Berger*/
class Move {
    
    /** Returns a new Move.
     *  MOVE is two integers 1-64, 
     *  PIECE is an integer 2-7 representing the index in the bitboard that needs to be changed
     *  PLAYER is the current player.
     *  CAPTURE true iff the move is a capture.*/
    Move(int[] move, int piece, Color player) {
        _move = move;
        _piece = piece;
        _player = player;
        _capture = false;
    }    

    public void setCapture(int piece) {
        _capture = true;
        _captured = piece;
    }

    public int[] getCoords() {
        return _move;
    }

    public int getPiece() {
        return _piece;
    }
    
    public boolean isWhite() {
        if (player == WHITE) {
            return true;
        }
        return false;
    }

    public boolean isCapture() {
        return _capture;
    }
    
    public int getCapture() {
        return _captured;
    }

    private int[] _move;
    private int _piece;
    private int _player;
    private boolean _capture;
    /** Bitboard position of capture piece*/
    private int _captured;
}