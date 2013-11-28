package deciduous;

import static deciduous.Color;

class Search {

    Search() {
        _boardMethods = new Board();
    }

    private int negamax(int alpha, int beta, int depth, Color p, long[] b) {
        if (depth == 0) {
            return quiesce(alpha, beta, p, b);
        }
        int max = LOW_SCORE;
        int[][] moves = _boardMethods.generateMoves(b)
        for (int move[] : moves) {
            b = make(b, move);
            int prosp = -negamax(-beta, -alpha, depth - 1, p.opposite(), b);
            b = unmake(b, move);
            max = Math.max(max, prosp);
            alpha = Math.max(alpha, prosp);
            if (alpha >= beta) {
                break;
            }
        }
        addToTable(b, max, depth);
        return max;
    }

    /** Checks to make sure the opponent doesn't have a devastating reply.*/
    private int queisce(int alpha, int beta, Color p, long[] b) {

    }

    /** Adds board state to the hash table.
     *  Only updates if DEPTH > the current depth*/
    private void addToTable(long[] b, int score, int depth) {

    }




    private Board _boardMethods;

}