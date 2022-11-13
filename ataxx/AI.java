/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.HashMap;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.*;

/**
 * A Player that computes its own moves.
 *
 * @author Sasha L.
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 4;
    /**
     * A position magnitude indicating a win (for red if positive, blue
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     * a random-number generator for use in move computations.  Identical
     * seeds produce identical behaviour.
     */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }

        return _lastFoundMove;
    }

    /**
     * The move found by the last call to the findMove method
     * above.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _foundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _foundMove. If the game is over
     * on BOARD, does not set _foundMove.
     */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best;
        best = null;
        int bestScore;
        bestScore = 0;
        HashMap<Integer, Move> allMoves = new HashMap();
        allMoves.clear();
        int side = Move.EXTENDED_SIDE;
        int index = 0;
        for (int i = side * 2 + 2; i < side * 9 - 2; i++) {
            if (board.get(i) == board.whoseMove()) {
                for (int a = -2; a < 3; a++) {
                    for (int b = -2; b < 3; b++) {
                        int tIndex = board.neighbor(i, a, b);
                        if (board.get(tIndex) == EMPTY) {
                            char col0 = (char) (('a' + (i % 11 - 2)));
                            char row0 = (char) (('1' + (i / 11 - 2)));
                            char col1 = (char) (('a' + ((tIndex) % 11 - 2)));
                            char row1 = (char) (('1' + ((tIndex) / 11 - 2)));
                            allMoves.put(index,
                                    Move.move(col0, row0, col1, row1));
                            index++;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < index; i++) {
            bestScore = -sense * WINNING_VALUE;
            board.makeMove(allMoves.get(i));
            int response = minMax(board, depth - 1,
                    false, -sense, alpha, beta);
            board.undo();
            if (best == null || (sense == 1 && response > bestScore)
                    || (sense == -1 && response < bestScore)) {
                bestScore = response;
                if (sense == 1) {
                    alpha = max(alpha, bestScore);
                } else if (sense == -1) {
                    beta = min(beta, bestScore);
                }
                best = allMoves.get(i);
                if (alpha >= beta) {
                    return bestScore;
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /**
     * Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     * won positions, and 0 for ties.
     */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }

        return board.redPieces() - board.bluePieces();
    }

    /**
     * Pseudo-random number generator for move computation.
     */
    private Random _random = new Random();
}
