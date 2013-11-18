package deciduous;

/** Library of operations for manipulating the board representation.
 *  This class does not hold the board representation, it merely contains
 *  static methods for doing things with it.
 *  The board representation itself is a bitboard 
 *  @author Andrew Berger */
class Board {
    
    Board() {
        //fixme
    }


    /** Returns an array of longs representing the board state.
     *  Each long treated as a 64 bit word 
     *  Bit indexing: 
     *     Least significant digit is 0
     *     MSD is 63
     *   
     *  Square ordering:
     *
     *  1: A B C D E F G H | 0 1 2 3 4 5 6 7 
     *  2: A B C D E F G H | 8 9 10 11 12 13 14 15 
     *  3: A B C D E F G H | 16 17 18 19 20 21 22 23*
     *  ...
     *  8: A B C D E F G H | 56 57 58 59 60 61 62 63
     * 
     *  Array contents:
     *
     *  0:White pieces
     *  1:Black pieces
     *  2:pawns
     *  3:bishops
     *  4:knights
     *  5:rooks
     *  6:kings
     *  7:queens
     *  8:white visibility 
     *  9:black visibility*/
    public long[] initBitBoard() {
        long board[] = new long[9];
        board[0] = WHITE_PIECES;
        board[1] = BLACK_PIECES;
        board[2] = PAWNS:
        board[3] = BISHOPS;
        board[4] = KNIGHTS;
        board[5] = ROOKS;
        board[6] = KINGS;
        board[7] = QUEENS:
    }

    

}
    