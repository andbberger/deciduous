/** TODO:
 *
 *  -how to handle promotions?????
 *  -implement enPassant
 *  -Use built in enum.ordinal instead of index() method
 *  -write tests
 *  -write more tests
 *  -write more tests
 *  -write pawn move gen*/



package deciduous;

import java.util.ArrayList;
import java.util.Random;

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
        knightAttacks = new long[64];
        initKnightAttacks();
        Random rgen = new Random();
        for (int k = 0; k < 9; k++) {
            hMultiplers[k] = rgen.nextLong();
        }
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
     *  3: A B C D E F G H | 16 17 18 19 20 21 22 23
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
        board[2] = PAWNS;
        board[3] = BISHOPS;
        board[4] = KNIGHTS;
        board[5] = ROOKS;
        board[6] = KINGS;
        board[7] = QUEENS;
    }

    /** Core operation. 
     *  MOVE is of format {currSquare, finalSquare}
     *  Returns the modified board
     *  Doesn't yet handle promotions or en passant, of course*/
    public static long[] make(long[] board, Move m) {
        if (m.isWhite()) {
            board[0] ^= (1 << m.getCoords()[0]);
            board[0] ^= (1 << m.getCoords()[1]);
            if (m.isCapture()) {
                board[1] ^= (1 << m.getCoords()[1]);
            }             
        } else {
            board[1] ^= (1 << m.getCoords()[0]);
            board[1] ^= (1 << m.getCoords()[1]);
            if (m.isCapture()) {
                board[0] ^= (1 << m.getCoords()[1]);
            }             
        }
        if (m.isCapture()) {
            board[m.getCapture()] ^= (1 << m.getCoords()[1]);
        }
        board[m.getPiece()] ^= (1 << m.getCoords()[0]);
        board[m.getPiece()] ^= (1 << m.getCoords()[1]);
    }


    /** MOVE as in the same format as make. 
     *  The inverse operation of make. */
    public static long[] unmake(long[] board, Move u) {
        //delightfully xor is it's own inverse operation and this will
        //work until I get fancy with en passant 
        return make(board, u)
    }


    /** First attempt at designing hash function. 
     *  This one will take the rather naive approach of multiplying
     *  each board state by a random long and xoring them together.
     *  There is no incremental update scheme, I hope this will be fast enough.
     *  Collision testing must be performed.*/
    public static long hash(long[] board) {
        long h = 0;
        for (int r = 0; r < 9; r++) {
            h ^= board[r] * hMultipliers[r];
        }
        return h;
    }

    /** Returns list of pseudo legal moves 
     *  Doesn't distinguish between captures and quiet moves*/
    public static ArrayList<int[]> generateMoves(long[] board, Color player) {
        List<int[]> moves = new ArrayList<int[]>();
        moves.addAll(generateBishopMoves(board, player));
        moves.addAll(generateRookMoves(board, player));
        moves.addAll(generatePawnMoves(board, player));
        moves.addAll(generateQueenMoves(board, player));
        moves.addAll(generateKnightMoves(board, player));
        moves.addAll(generateKingMoves(board, player));
    }
    
    public static ArrayList<int[]> generateCaptures(long[] board, Color player) {
        List<int[]> moves = new ArrayList<int[]>();
        moves.addAll(generateBishopCaptures(board, player));
        moves.addAll(generateRookCaptures(board, player));
        moves.addAll(generateKnightCaptures(board, player));
        moves.addAll(generateQueenCaptures(board, player));
        moves.addAll(generateKingCaptures(board, player));
        moves.addAll(generatePawnCaptures(board, player));
    }

    /** Returns an ArrayList of all non captures moves rooks on BOARD of COLOR can make. */
    public static ArrayList<Moves> generateRookMoves(long[] board, Color player) {
        ArrayList<Moves> rookMoves = new ArrayList<Moves>();
        long rooks = board[player.index()] & board[5];
        while (rooks != 0) {
            int rookPos = bitscanForward(rooks);
            long rookPseudos = rookMoves(board, rookPos, player) & ~board[player.opposite().index()];
            rookMoves.addAll(parseMoves(rookPos, rookPseudos, player, 5));
            rooks ^= (1 << rookPos);
        }
        return rookMoves;
    }

    /** Returns an ArrayList of all non capture moves bishops on BOARD of COLOR can make.  */
    public static ArrayList<Moves> generateBishopMoves(long[] board, Color player) {
        ArrayList<Moves> bishopMoves = new ArrayList<Moves>();
        long bishops = board[player.index()] & board[3];
        while (bishops != 0) {
            int bishopPos = bitscanForward(bishops);
            long bishopPseudos = bishopMoves(board, bishopPos, player) & ~board[player.opposite().index()];
            bishopMoves.addAll(parseMoves(bishopPos, bishopPseudos, player, 3);
            bishops  ^= (1 << bishopPos);
        }
        return bishopMoves;
    }

    public static ArrayList<Moves> generateKnightMoves(long[] board, Color player) {
        ArrayList<Moves> knightMoves = new ArrayList<Moves>();
        long knights = board[player.index()] & board[4];
        while (knights != 0) {
            int knightPos = bitscanForward(knights);
            long knightPseudos =  knightAttacks[knightPos] & ~board[player.index()] & ~board[player.opposite().index()];
            knightMoves.addAll(parseMoves(knightPos, knightPseudos, player, 4));
            knights ^= (1 << knightPos);
        }
        return knightMoves;
    }

    /** Returns an ArrayList of all non capture moves queens on BOARD of COLOR can make. */
    public static ArrayList<Moves> generateQueenMoves(long[] board, Color player) {
        ArrayList<Moves> queenMoves = new ArrayList<Moves>();
        long queens = board[player.index()] & board[7];
        while (queens != 0) {
            int queenPos = bitscanForward(queen);
            long queenPseudos = queenMoves(board, queenPos, player) & ~board[player.opposite().index()];
            queenMoves.addAll(parseMoves(queenPos, queenPseudos, player, 7));
        }
        return queenMoves;
    }

    /** Returns an ARrayList of all non capture moves the king on BOARD of COLOR can make. */
    public static ArrayList<Moves> generateKingMoves(long[] board, Color player) {
        long king = board[player.index()] & board[6];
        long kingPos = bitscanForward(king);
        long kingPseudos = kingMoves(board, player) & ~board[player.opposite().index()];
        return parseMoves(kingPos, kingPseudos, player, 6);
    } 

    public static ArrayList<Moves> generatePawnMoves(long[] board, Color player) {
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
        ArrayList<Moves> pMoves = new ArrayList<Moves>();
        for (int[] m : pawnMoves) {
            pMoves.add(new Move(m, 2, player));
        }
        return pMoves;
    }

    public static ArrayList<Move> generateRookCaptures(long[] board, Color player) {
        ArrayList<Move> rookCaptures = new ArrayList<Move>();
        long rooks = board[player.index()] & board[5];
        while (rooks != 0) {
            int rookPos = bitscanForward(rooks);
            long rookPseudos = rookMoves(board, rookPos, player) & board[player.opposite().index()];
            rookCaptures.addAll(parseCaptures(rookPos, rookPseudos, player, 5, board));
            rooks ^= (1 << rookPos);
        }
        return rookCaptures;
    }

    public static ArrayList<Move> generateBishopCaptures(long[] board, Color player) {
        ArrayList<Move> bishopCaptures = new ArrayList<Move>();
        long bishops = board[player.index()] & board[3];
        while (bishops != 0) {
            int bishopPos = bitscanForward(bishops);
            long bishopPseudos = bishopMoves(board, bishopPos, player) & board[player.opposite().index()];
            bishopCaptures.addAll(parseCaptures(bishopPos, bishopPseudos, plaer, 3));
            bishops ^= (1 << bishopPos);
        }
        return bishopCaptures;
    }

    public static ArrayList<Move> generateKnightCaptures(long[] board, Color player) {
        ArrayList<Move> knightCaptures = new ArrayList<Move>();
        long knights = board[player.index()] & board[4];
        while (knights != 0) {
            int knightPos = bitscanForward(knights);
            long knightPseudos = knightAttacks[knightPos] & board[player.opposite().index()];
            knightCaptures.addAll(parseCaptures(knightPos, knightPseudos, player, 4, board));
            knights ^= (1 << knightPos);
        }
        return knightCaptures;
    }
 
    public static ArrayList<Move> generateQueenCaptures(long[] board, Color player) {
        ArrayList<Move> queenMoves = new ArrayList<Move>();
        long queens = board[player.index()] & board[7];
        while (queens != 0) {
            long queenPos = bitscanForward(queen);
            long queenPseudos = queenMoves(board, queenPos, player) & board[player.opposite().index()];
            queenMoves.addAll(parseCaptures(queenPos, queenPseudos, player, 7, board));
        }
                
    }

    public static ArrayList<int[]> generateKingCaptures(long[] board, Color player) {
        long king = board[player.index()] & board[6];
        long kingPos = bitscanForward(king);
        long kingPseudos = kingMoves(board, player) & board[player.opposite().index()];
        return parseCaptures(kingPos, kingPseudos, player, 6, board);
    }

    public static ArrayList<int[]> generatePawnCaptures(long[] board, Color player) {
        //this one is more complicated 
    }
    

    /** Commonly used operation in move gen.
     *  Returns the arrayList of moves. 
     *  PIECE is an integer 2-7 representing the type of the piece that moves. */
    public static ArrayList<Move> parseMoves(int piecePos, long moves, Color player, int piece) {
        ArrayList<Move> movePairs = new ArrayList<Move>();
        while (moves != 0) {
            int[] m = new int[2];
            m[0] = piecePos;
            m[1] = bitscanForward(moves);
            Move myMove = new Move(m, piece, player);
            movePairs.add(myMove);
            moves ^= (1 << m[1]);
        } 
        return movePairs;
    }

    /** Commonly used operation in move gen.
     *  Returns the ArrayList of capture (move objects)
     *  PIECE is an integer 2-7 representing the type of the piece that moves. */
    public static ArrayList<Move> parseCaptures(int piecePos, long moves, Color player, int piece, long[] board) {
        ArrayList<Move> captures = new ArrayList<Move>();
        while (moves != 0) {
            int m = new int[2];
            m[0] = piecePos;
            m[1] = bitScanForward(moves);
            Move myMove =  new Move(m, piece, player);
            for (int p = 2; p <= 7; p++) {
                if ((board[p]  & (1 << m[1])) != 0) {
                    myMove.setCapture(p);
                    break;
                } 
            }
            captures.add(myMove);
            moves ^= (1 << m[1]);
        }
        return captures;
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

    /** Returns the state representing all moves the rook on SQUARE can make, including captures.*/
    public static long rookMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, 1) | rayAttack(board, square, -1) | rayAttack(board, square, 8) | rayAttack(board, square, -8);
        //ray attack includes an obstacle at the end, possibly of the same color
        return uncheckedMoves & ~board[player.index()];
    }

    /** Returns the state representing all moves the bishop on SQUARE can make, including captures.*/
    public static long bishopMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, -7) | rayAttack(board, square, 7) | rayAttack(board, square, -9) | rayAttack(board, square, 9);
        return uncheckedMoves & ~board[player.index()];
    }


    /** Returns the state representing all moves the queen on SQUARE can make, including captures. */
    public static long queenMoves(long[] board, int square, Color player) {
        long uncheckedMoves = rayAttack(board, square, -7) | rayAttack(board, square, 7) | rayAttack(board, square, -9) | rayAttack(board, square, 9) | rayAttack(board, square, 1) | rayAttack(board, square, -1) | rayAttack(board, square, 8) | rayAttack(board, square, -8);
        return uncheckedMoves & ~board[player.index()];
    }

    public static long kingMoves(long[] board, Color player) {
        long king = board[player.index()] & board[6];
        long rect = (king & maskFile[0] << 1) & (king << 8) & (king & maskFile[7] >>> 1) & (king >>> 8);
        long diag = (king & maskFile[7] << 7) & (king & maskFile[0] << 9) & (king & maskFile[0] >>> 7) & (king & maskFile[7] >>> 9);
        return rect & diag;
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


    /** Fills up our very convenient lookup table
     *  Knight compass rose:
     *
     *        noNoWe    noNoEa
     *            +15  +17
     *             |     |
     *noWeWe  +6 __|     |__+10  noEaEa
     *              \   /
     *               >0<
     *           __ /   \ __
     *soWeWe -10   |     |   -6  soEaEa
     *             |     |
     *            -17  -15
     *        soSoWe    soSoEa
     **/
    private void initKnightAttacks() {
        for (int s = 0; s < 64; s++) {
            long knight = (1 << s);
            knightAttacks[s] = knight;
            knightAttacks[s] |= (knight << 17) & clearFile[0];
            knightAttacks[s] |= (knight << 10) & (clearFile[0] & clearFile[1]);
            knightAttacks[s] |= (knight >>> 6) & (clearFile[0] & clearFile[1]);
            knightAttacks[s] |= (knight >>> 15) & clearFile[0];
            knightAttacks[s] |= (knight << 15) & clearFile[7];
            knightAttacks[s] |= (knight << 6) & (clearFile[6] & clearFile[7]);
            knightAttacks[s] |= (knight >>> 10) & (clearFile[6] & clearFile[7]);
            knightAttacks[s] |= (knight >>> 17) & clearFile[7];
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
    private static long fileRange(int start, int end) {
        long result = 0;
        for (int i = start; i < end; i++) {
            result |= fillFile(i);
        }
        return result;
    }

    /** 64 wide array with knight attacks for each square*/
    private static final long[] knightAttacks;

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


    private static final long[] hMultipliers;

    /** Utility function for the move generator */
    private static int[][] arrListToPrim(ArrayList<int[]> m) {
        int[][] prim = new int[m.size()][2];
        for (int i = 0; i < m.size(); i++) {
            prim[i] = m.get(i);
        }
        return prim;
    }
}
    