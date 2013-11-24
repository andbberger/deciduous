/** TODO:
 *
 *  -how to handle promotions?????
 *  -implement enPassant
 *  -change naive implementation of generateMoves:
 *     -generateQuietMoves
 *     -generateCaptures*/



package deciduous;

import java.util.ArrayList;

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
     *  8: ? 
     *  9: ? */
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

    /** Returns list of pseudo legal moves 
     *  Doesn't distinguish between captures and quiet moves*/
    public static ArrayList<int[]> generateMoves(long[] board, Color player) {
        //218 is the max number of moves per position (??)
        ArrayList<int[]> moves = new ArrayList<int[]>();
        long myPieces = board[player.index()];
        long rooks = myPieces & board[5];
        while (rooks != 0) {
            int rook = bitscanForward(rooks);
            long rookPseudos = rookMoves(board, rook, player);
            while (rookPseudos != 0) {
                int[] m = new int[2];
                m[0] = rook;
                m[1] = bitscanForward(rookPseudos);
                moves.add(m);
                rooks ^= (1 << rook);
                rookPseudos ^= (1 << m[1]);
            }
        }
        long bishops = myPieces & board[3];
        while (bishops != 0) {
            int bishop = bitscanForward(bishops);
            long bishopPseudos = bishopMoves(board, bishop, player);
            while (bishopPseudos != 0) {
                int m = new int[2];
                m[0] = bishop;
                m[1] = bitscanForward(bishopPseudos);
                moves.add(m);
                bishops  ^= (1 << bishop);
                bishopPseudos ^= (1 << m[1]);
            }
        }
        pawnMoves = generatePawnMoves(board, player);
        
    }

    public static ArrayList<int[]> generatePawnMoves(long[] board, Color player) {
        ArrayList<int[]> pawnMoves = new ArrayList<int[]>();
        int file = 0;
        int pushIncr, lcIncr, rcIncr;
        long pushes, dPushes, lCapt, rCapt;
        if (player == WHITE) {
            pushIncr = -8;
            lcIncr = -7;
            rcIncr = -9;
            pushes = wPawnPushes(board);
            dPushes = wPawnDoublePushes(board);
            lCapt = wPawnLeftCaptures(board);
            rCapt = wPawnRightCaptures(board);
        } else {
            pushIncr = 8;
            //this might be wrong!! 
            lcIncr = 9;
            rcIncr = 7;
            pushes = bPawnPushes(board);
            dPushes = bPawnDoublePushes(board);
            lCapt = bPawnLeftCaptures(board);
            rCapt = bPawnRightCaptures(board);
        }
        while (pushes != 0) {
            long pushFile = pushes & maskFile(file);
            long dPushFile = dPushes & maskFile(file);
            if (pushFile != 0) {
                int[] m = new int[2];
                m[1] = bitscanForward(pushFile);
                m[0] = m[1] + pushIncr;
                pawnMoves.add(m);
                pushes ^= (1 << m[1]);
            }
            if (dPushFile != 0) {
                int[] d = new int[2];
                //computing extraneous information all is needed is an array of booleans 
                m = pawnMoves.get(pawnMoves.size());
                d[0] = m[0];
                d[1] = m[1] - pushIncr;
                pawnMoves.add(d);
            }
            file += 1;
        }
        while (lCapt != 0) {
            int[] l = new int[2];
            l[1] = bitscanForward(lCapt);
            l[0] = l[1] + lcIncr;
            lCapt ^= (1 << l[1]);
        }
        while (rCapt != 0) {
            int[] r = new int[2];
            r[1] = bitscanForward(rCapt);
            r[0] = r[1] + rcIncr;
            rCapt ^= (1 << r[1]);
        }
        return pawnMoves;
    }

    /** Returns the number of bits flipped on in state */
    public static int cardinality(long state) {
        
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


    /** Very important routine
     *  Naive implementation with java library call
     *  Deserving of some de bruijn magic*/
    public static int bitscanLSD(long state) {
        long lsb = state & -state;
        return Long.numberOfTrailingZeroes(lsb);
    }

    public static int bitscanMSD(long state) {
        //not sure how to implement this simply
    }

    
    /** Returns the state representing the squares sliding piece on SQUARE can move
     *  Very naive implementation and there are definitely fancier ways of doing sliding move generation 
     *  I think there is a bug when the board is empty
     *  DIR only has certain allowed values refer to ray doc*/
    private static long rayAttack(long[] board, int square, int dir) {
        long r = ray(square, dir);
        long occupancy = board[0] & board[1];
        long obstacles = r & occupancy;
        //turn on MSB?
        long unavailable = ray(bitscanLSD(obstacles), dir);
        return  r ^ unavailable;
    }

    /** Returns the board state of possible single pawn pushes 
     *  Does not treat captures or promotion*/
    public static long wPawnPushes(long[] board) {
        long pawns = board[0] & board[2];
        long occ = board[0] & board[1];
        return (pawns << 8) & ~occ;
    }

    /** Returns the board state of possible single pawn pushes 
     *  Does not treat captures or promotion*/
    public static long bPawnPushes(long[] board) {
        long pawns = board[1] & board[2];
        long occ = board[0] & board[1];
        return (pawns >>> 8) & ~occ;
    }

    public static long wPawnDoublePushes(long[] board) {
        long pawns = board[0] & board[2];
        long occ = board[0] & board[1];
        long canJump = pawns & maskRank[1];
        return (canJump << 16) & ~occ;
    }

    public static long bPawnDoublePushes(long[] board) {
        long pawns = board[1] & board[2];
        long occ = board[0] & board[1];
        long canJump = pawns & maskRank[6];
        return (canJump >>> 16) & ~occ;
    }


    /** Gets state of possible right captures. 
     *  Does not calculate en pasant */    
    public static long wPawnRightCaptures(long[] board) {
        long pawns = board[0] & board[2];
        pawns &= clearFile[0];
        long rightMoves = pawns << 9;
        return rightMoves & board[1];
    }

    /** Gets state of possible right captures. 
     *  Does not calculate en pasant */
    public static long bPawnRightCaptures(long[] board) {
        long pawns = board[1] & board[2];
        pawns &= clearFile[0];
        long rightMoves = pawns >>> 7;
        return rightMoves & board[0];
    }

    /** Gets state of possible left captures
     *  Does not calculate en passant */
    public static long wPawnLeftCaptures(long[] board) {
        long pawns = board[0] & board[2];
        pawns &= clearFile[7];
        long leftMoves = pawns << 7;
        return leftMoves & board[1];
    }

    /** Gets state of possible left captures
     *  Does not calculate en passant */
    public static long bPawnLeftCaptures(long[] board) {
        long pawns = board[1] & board[2];
        pawns &= clearFile[7];
        long leftMoves = pawns >>> 9;
        return leftMoves * board[0];
    }

    /** Returns the state representing all moves the rook on SQUARE can make*/
    public static long rookMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, 1) | rayAttack(board, square, -1) | rayAttack(board, square, 8) | rayAttack(board, square, -8)
        //I believe this is necessary because the result of the ray attacks
        //sometimes would have the attack ending at a friendly piece
        return uncheckedMoves & ~board[player.index()];
    }

    public static long bishopMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, -7) | rayAttack(board, square, 7) | rayAttack(board, square, -9) | rayAttack(board, square, 9);
        return uncheckedMoves & ~board[player.index()];
    }

    public static long queenMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, -7) | rayAttack(board, square, 7) | rayAttack(board, square, -9) | rayAttack(board, square, 9) | rayAttack(board, square, 1) | rayAttack(board, square, -1) | rayAttack(board, square, 8) | rayAttack(board, square, -8);
        return uncheckedMoves & ~board[player.index()];
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


    /** Utility function for the move generator */
    private static int[][] arrListToPrim(ArrayList<int[]> m) {
        int[][] prim = new int[m.size()][2];
        for (int i = 0; i < m.size(); i++) {
            prim[i] = m.get(i);
        }
        return prim;
    }
}
    