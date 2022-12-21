/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

import static ataxx.PieceColor.EMPTY;
import static ataxx.PieceColor.RED;
import static ataxx.Board.EXTENDED_SIDE;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Oumar Balde
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
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

    /** Return a move for me from the current position, assuming there
     *  is a move. */
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

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best;
        best = null; int bestScore;
        bestScore = 0;
        ArrayList<Move> movesArrayList = possibleMoves(board);
        if (sense == 1) {
            bestScore = -INFTY;
            for (Move move: movesArrayList) {
                if (board.legalMove(move)) {
                    Board copyBoard = new Board(board);
                    copyBoard.makeMove(move);
                    int eval = minMax(copyBoard, depth - 1,
                            false, -1, alpha, beta);
                    if (eval > bestScore) {
                        best = move;
                        bestScore = eval;
                        alpha = max(eval, alpha);
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = best;
            }
        } else {
            bestScore = INFTY;
            for (Move move: movesArrayList) {
                if (board.legalMove(move)) {
                    Board copyBoard = new Board(board);
                    copyBoard.makeMove(move);
                    int eval = minMax(copyBoard, depth - 1,
                            false, 1, alpha, beta);
                    if (eval < bestScore) {
                        best = move;
                        bestScore = eval;
                        beta = min(eval, beta);
                    }
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (saveMove) {
                _lastFoundMove = best;
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }



    /**
     * Helper function that returns the column corresponding to
     * a given index on the board.
     * @param mod
     */
    private char getCol(int mod) {
        HashMap<Integer, Character> map = new HashMap<>();
        for (int i = 2, ch = 'a'; i <= 8; i++, ch++) {
            map.put(i, (char) (ch));
        }
        return map.get(mod);
    }

    /**
     * Helper function that returns all the possible moves that
     * the current player is able to make on the current board.
     * @param board
     */
    private ArrayList<Move> possibleMoves(Board board) {
        ArrayList<Move> movesArrayList = new ArrayList<>();
        movesArrayList.add(Move.pass());

        char c0, c1, r0, r1;

        for (int sq = 0; sq < EXTENDED_SIDE * EXTENDED_SIDE; sq++) {
            if (board.get(sq) == board.whoseMove()) {
                for (int i = sq - EXTENDED_SIDE * 2;
                     i <= sq + EXTENDED_SIDE * 2; i += EXTENDED_SIDE) {
                    for (int j = i - 2; j <= i + 2; j++) {
                        if (board.get(j) == EMPTY) {
                            c0 = getCol(sq % EXTENDED_SIDE);
                            c1 = getCol(j % EXTENDED_SIDE);
                            r0 = (char) ('0'
                                    + Math.floorDiv(sq, EXTENDED_SIDE) - 1);
                            r1 = (char) ('0'
                                    + Math.floorDiv(j, EXTENDED_SIDE) - 1);
                            movesArrayList.add(Move.move(c0, r0, c1, r1));
                        }
                    }
                }
            }
        }
        return movesArrayList;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
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

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}
