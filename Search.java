package deciduous;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import static deciduous.Color;

class Search {

    Search() {
        _board = new Board();
    }

    private int negamax(int alpha, int beta, int depth, Color p, long[] b) {
        if (depth == 0) {
            return quiesce(alpha, beta, p, b);
        }
        int max = LOW_SCORE;
        ArrayList<int[]> moves = _board.generateMoves(b)
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
        int curr_eval = _eval.staticEval(b);
        if (curr_eval >= beta) {
            return beta;
        } else if (alpha < curr_eval) {
            alpha = curr_eval;
        }
        ArrayList<int[]> captures = _board.generateCaptures(b);
        for (int[] capture : captures) {
            b = make(b, capture);
            int score = -queisce(-beta, -alpha);
            b = unmake(b, capture);
            if (score >= beta) {
                return score;
            } else if (score > alpha) {
                alpha = score;
            }
        }
        return alpha;
    }

    /** Adds board state to the hash table.
     *  Only updates if DEPTH > the current depth*/
    private void addToTable(long[] b, int score, int depth) {

    }

    /** The instance of boardMethods. */
    private Board _board;
    /** The instace of eval methods */
    private Eval _eval;
    /** Large hashMap holding all of our previously visited board states
     *  Key is the board hash
     *  Value is yet to be determined but will be at least depth, score and the move we picked last time*/
    private Map<Integer, int[]> _table;
    /** For min max purposes. Be careful of mod arith. */
    private int HIGH_SCORE = 1000000000;
    /** For min max purposes. Be careful of mod arith. */
    private int LOW_SCORE = -1000000000;
    /** Bigger than the other one still not close to max */
    private int TOP = 2000000000;
    /** Smaller than the other still not close to min */
    private int BOT = -2000000000;

}