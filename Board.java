package deciduous;

import static deciduous.Color;

/** Library of operations for manipulating the board representation.
 *  This class does not hold the board representation, it merely contains
 *  static methods for doing things with it.
 *  The board representation itself is a bitboard 
 *  @author Andrew Berger */
class Board {
    
    Board() {
        clearRank = new long[8];
        clearFile = new long[8];
        maskRank = new long[8];
        maskFile = new long[8];
        for (int i = 0; i < 8; i++) {
            maskRank[i] = fillRank(i);
            maskFile[i] = fillFile(i);
            clearRank[i] = ~maskRank[i];
            clearFile[i] = ~maskFile[i];
        }
        north = new long[64];
        northEa = new long[64];
        west = new long[64];
        southWe = new long[64];
        south = new long[64];
        southEa = new long[64];
        east = new long[64];
        northWe = new long[64];
        initRays();
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
    
    /** Destructively get the Nth bit of STATE.
     *  Return true iff it is 1 */
    public static boolean get(long state, int n) {
        return (state >>> n) == 1;
    }
    
    /** Sets the nth bit to BIT 
     *  Returns the new STATE. */
    public static long set(long state, boolean bit, int n) {
        //lazy implementation
        if (get(state, n) == bit) {
            return state;
        } else {
            if (get(state, n) == 1) {
                return state - (1 << n);
            } else {
                return state + (1 << n);
            }
        }
    }


    /** Returns the board state of possible pawn positions 
     *  Does not treat captures or promotion*/
    public static long pawnPushes(long[] board, Color player) {    
        int sign = player.sign();
        long pawns = board[player.index()] & board[2];
        long canJump = pawns & PAWNS;
        //not sure shift takes negative arguments 
        return (pawns << sign * 8) | (canJump << sign * 16);
    }

    /** Gets state of possible right captures. 
     *  Does not calculate en pasant */
    public static long pawnRightCaptures(long[] board, Color player) {
        int sign = player.sign();
        long pawns = board[player.index()] & board[2];
        long noLeft = pawns & clearFile[0];
        long rightMoves = noLeft << (sign * 8 + 1);
        return rightMoves & board[player.opposite().index()];
    }

    /** Gets state of possible left captures
     *  Does not calculate en passant */
    public static long pawnLeftCaptures(long[] board, Color player) {
        int sign = player.sign();
        long pawns = board[player.index()] & board[2];
        long noRight = pawns & clearFile[7];
        long leftMoves = noRight << (sign * 8 - 1);
        return leftMoves & board[player.opposite().index()];
    }
    

    /** Returns the state with all of the bits from the starting point exclusive
     *  to the border 
     *
     *  Allowed values of orientation:
     *
     *   noWe         nort         noEa
     *          +7    +8    +9
     *              \  |  /
     *  west    -1 <-  0 -> +1    east
     *              /  |                        \
     *          -9    -8    -7
     *  soWe         sout         soEa
     *
     **/
    public static long ray(int square, int orientation) {
        switch (orientation) {
        case (8):
            return north[square];
        case (9):
            return northEa[square];
        case (1):
            return east[square];
        case (-7):
            return southEa[square];
        case(-8):
            return south[square];
        case(-9):
            return southWe[square];
        case(-1):
            return west[square];
        case (7):
            return northWe[square];
        default:
            throw new Exception("Orientation is not an allowed value");
        }
    }

    /** Called by the constructor. Fills in our ray tables */
    private void initRays() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int ind = rank * 8 + file;
                long vertical = maskFile[file] & clearRank[rank];
                long horizontal = maskRank[rank] & clearFile[rank];
                north[ind] = rankRange(rank, 7) & vertical;
                south[ind] = rankRange(0, rank) & vertical;
                west[ind] = fileRank(0, file) & horizontal;
                east[ind] = fileRank(file, 7) & horizontal;
            }
        }
        for (int i = 0; i < 64; i++) {
            northEa[i] = (1 << i);
            northWe[i] = (1 << i);
            southEa[i] = (1 << i);
            southWe[i] = (1 << i);
            for (int j = 0; j < 8; j++) {
                northEa[i] |= (northEa[i] & clearFile[7]) << 9;
                northWe[i] |= (northWe[i] & clearFile[0]) << 7;
                southEa[i] |= (southEa[i] & clearFile[7]) << -7;
                southWe[i] |= (southWe[i] & clearFile[0]) << -9;
            }
            northEa[i] &= ~(1 << i);
            northWe[i] &= ~(1 << i);
            southEa[i] &= ~(1 << i);
            southWe[i] &= ~(1 << i);
        }
    }
    
    /** Ranks correspond to numbers. RANK corresponds to letters
     *  0 : A
     *  1 : B
     *  And so on
     *  Returns the state with RANK filled
     *  Used to initialize tables*/
    private static long fillRank(int rank) {
        long result = 11111111;
        return (result << rank * 8);
    }

    /** Same doc as fillRank but for files */
    private static long fillFile(int file) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result |= (file << i * 8); 
        }
        return result;
    }

    /** Returns state filled from rank [start to end) */ 
    private static long rankRange(int start, int end) {
        long result = 0;
        for (int i = start; i < end; i++) {
            result |= fillRank(i);
        }
        return result;
    }

    /** Returns state filled from file [start to end) */ 
    private static long rankFile(int start, int end) {
        long result = 0;
        for (int i = start; i < end; i++) {
            result |= fillFile(i);
        }
        return result;
    }

    /** Each of the following four tables is of size 8.
     *  The entry at i performs the eponymous operation when &ed with a state*/
    private static final long[] clearRank;
    private static final long[] clearFile;
    private static final long[] maskRank;
    private static final long[] maskFile;

    /** Each entry is the eponymous ray for that square */
    private static final long[] north;
    private static final long[] northEa;
    private static final long[] west;
    private static final long[] southWe;
    private static final long[] south;
    private static final long[] southEa;
    private static final long[] east;
    private static final long[] northWe;
}
    