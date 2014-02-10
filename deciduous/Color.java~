package deciduous;

/** Enumeration clas for colors
 *  @author Andrew Berger */
enum Color {
    
    /** Possible colors */
    WHITE, BLACK;

    Color opposite() {
        switch(this) {
        case WHITE:
            return BLACK;
        case BLACK:
            return WHITE;
        }
    }

    /** Return 0 if WHITE, 1 if BLACK */
    int index() {
        switch(this) {
        case WHITE:
            return 0;
        case BLACK:
            return 1;
        }   
    }

    /** My direction for pawns */
    int sign() {
        switch(this) {
        case WHITE:
            return 1;
        case BLACK:
            return -1;
        }
    }

        

}