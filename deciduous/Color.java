package deciduous;

/** Enumeration clas for colors
 *  @author Andrew Berger */
enum Color {
    
    /** Possible colors */
    WHITE, BLACK;

    Color opposite() {
        if (this == WHITE) {
            return BLACK;
        } else {
            return WHITE;
        }
    }

    /** Return 0 if WHITE, 1 if BLACK */
    int index() {
        if (this == WHITE) {
            return 0;
        } else {
            return 1;
        }
    }

    /** My direction for pawns */
    int sign() {
        if (this == WHITE) {
            return 1;
        } else {
            return -1;
        }
    }

        

}