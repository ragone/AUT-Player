package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * My implementation of a GGP player implementing Monte Carlo Tree Search
 *
 * @author Alexander Ragone
 * @version 18/03/16
 */
public class AUTGamer extends StateMachineGamer {

    private int[] depth = new int[1];
    private StateMachine theMachine;

    /**
     * Defines the algorithm that the player uses to select their move.
     * @param timeout time in milliseconds since the era when this function must return
     * @return Move - the move selected by the player
     * @throws TransitionDefinitionException
     * @throws MoveDefinitionException
     * @throws GoalDefinitionException
     */
    @Override
    public Move stateMachineSelectMove(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        // Get the state machine
        theMachine = getStateMachine();
        long start = System.currentTimeMillis();
        long finishBy = timeout - 1000;

        // Get the moves available
        List<Move> moves = theMachine.getLegalMoves(getCurrentState(), getRole());
        Move selection = moves.get(0);

        UCTNode currentNode = new UCTNode(null);

        // If more than one move available choose best move, otherwise play only legal move
        if (moves.size() > 1) {
            int[] moveTotalScore = new int[moves.size()];
            int[] moveTotalAttempts = new int[moves.size()];

            // While there is time left, send probes out to random terminal states and get score
            int index = 0;
            while (System.currentTimeMillis() < finishBy) {
                int score = deployProbeFromMove(getCurrentState(), moves.get(index));
                moveTotalScore[index] += score;
                moveTotalAttempts[index] += 1;
                index = (index + 1) % moves.size();
            }

            // Compute the average score for each move.
            double[] moveAveragePoints = new double[moves.size()];
            for (int i = 0; i < moves.size(); i++) {
                moveAveragePoints[i] = (double)moveTotalScore[i] / moveTotalAttempts[i];
                System.out.println(moves.get(i) + ": " + moveAveragePoints[i]);
            }

            // Find the move with the best average score.
            int bestMove = 0;
            double bestMoveScore = moveAveragePoints[0];
            for (int i = 1; i < moves.size(); i++) {
                if (moveAveragePoints[i] > bestMoveScore) {
                    bestMoveScore = moveAveragePoints[i];
                    bestMove = i;
                }
            }
            selection = moves.get(bestMove);
        }

        long stop = System.currentTimeMillis();
        notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
        return selection;
    }

    /**
     * Deploy a probe that return the score from a random outcome of the end game
     * @param state
     * @param move
     * @return
     */
    private int deployProbeFromMove(MachineState state, Move move) {
        try {
            List<Move> randomMoves = getRandomJointMove(state, getRole(), move);
            MachineState randomState = theMachine.getNextState(state, randomMoves);
            MachineState randomFinalState = getRandomFinalState(randomState, depth);
            System.out.println("MOVE: " + move + " - ROLE: " + getRole() + " - SCORE: " + theMachine.getGoal(randomFinalState, getRole()));
            return theMachine.getGoal(randomFinalState, getRole());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Returns a terminal state derived from repeatedly making random joint moves
     * until reaching the end of the game.
     *
     * @param depth an integer array, the 0th element of which will be set to
     * the number of state changes that were made to reach a terminal state.
     */
    private MachineState getRandomFinalState(MachineState state, final int[] depth) throws TransitionDefinitionException, MoveDefinitionException {
        int currentDepth = 0;
        while(!theMachine.isTerminal(state)) {
            currentDepth++;
            state = theMachine.getNextState(state, getRandomJointMoves(state));
        }
        if(depth != null)
            depth[0] = currentDepth;
        return state;
    }

    /**
     * Returns a random joint move from among all the possible joint moves in
     * the given state.
     */
    private List<Move> getRandomJointMoves(MachineState state) throws MoveDefinitionException
    {
        List<Move> randomMoves = new ArrayList<>();
        for (Role role : theMachine.getRoles()) {
            randomMoves.add(getRandomMove(state, role));
        }

        return randomMoves;
    }

    /**
     * Returns a random joint move when one move is already determined by one of the roles.
     */
    private List<Move> getRandomJointMove(MachineState state, Role role, Move move) throws MoveDefinitionException
    {
        List<Move> randomMoves = new ArrayList<>();
        for (Role r : theMachine.getRoles()) {
            if (r.equals(role)) {
                randomMoves.add(move);
            } else {
                randomMoves.add(getRandomMove(state, r));
            }
        }

        return randomMoves;
    }

    /**
     * Returns a random move for the role in the given state.
     */
    private Move getRandomMove(MachineState state, Role role) throws MoveDefinitionException
    {
        List<Move> legals = theMachine.getLegalMoves(state, role);
        return legals.get(new Random().nextInt(legals.size()));
    }

    /**
     * Defines any actions that the player takes upon the game cleanly ending.
     */
    @Override
    public void stateMachineStop() {
        // Cleanup when the match ends normally.
    }

    /**
     * Defines any actions that the player takes upon the game abruptly ending.
     */
    @Override
    public void stateMachineAbort() {
        // Cleanup when the match ends abruptly.
    }

    /**
     * Defines any actions to do before the game starts.
     * @param g the game to be played
     * @param timeout time until game start
     * @throws GamePreviewException
     */
    @Override
    public void preview(Game g, long timeout) throws GamePreviewException {
        // Game previewing
    }

    /**
     * Get the name of the player class
     * @return name of player
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public DetailPanel getDetailPanel() {
        return new SimpleDetailPanel();
    }

    /**
     * Defines which state machine this gamer will use.
     * @return
     */
    @Override
    public StateMachine getInitialStateMachine() {
        return new CachedStateMachine(new ProverStateMachine());
    }

    /**
     * Defines the metagaming action taken by a player during the START_CLOCK
     * @param timeout time in milliseconds since the era when this function must return
     * @throws TransitionDefinitionException
     * @throws MoveDefinitionException
     * @throws GoalDefinitionException
     */
    @Override
    public void stateMachineMetaGame(long timeout) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

    }
}
