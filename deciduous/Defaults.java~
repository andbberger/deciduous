package deciduous;

public class Defaults {
    
    /**Initial configuration of white */
    final static long WHITE_PIECES = 18446462598732840960;
    /**Initial configuration of black */
    final static long BLACK_PIECES = 65535;
    /** Initial configuration of pawns */
    final static long PAWNS = 71776119061282560;
    /** Initial configuration of bishops */
    final static long BISHOPS = 2594073385365405732;
    /** Initial confiuration of knights */
    final static long KNIGHTS = 4755801206503243842;
    /** The initla location of rooks */
    final static long ROOKS = 9295429630892703873;
    /** The initial location of the kings */
    final static long KINGS = 576460752303423496;
    /** The initial location of the queens */;
    final static long QUEENS = 1152921504606846992;
    /** Useful number. All bits filled. */
    final static long FULL = 18446744073709551615;
    /** Only first col filled */
    final static long FIRST_COL = fillCol(1);
    /** Only last col filled */
    final static long LAST_COL = fillCol(8);

    /** Returns mask with COL bits turned on. */
    private long fillCol(int col) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            res |= (col << i * 8); 
        }
    }
}