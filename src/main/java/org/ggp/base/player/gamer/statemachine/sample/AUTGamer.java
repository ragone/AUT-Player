package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.apps.player.detail.DetailPanel;
import org.ggp.base.apps.player.detail.SimpleDetailPanel;
import org.ggp.base.player.gamer.event.GamerSelectedMoveEvent;
import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.player.gamer.statemachine.StateMachineGamer;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
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

    private StateMachine theMachine;
    private long start;
    private long finishBy;
    private int roleIndex;

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
        roleIndex = theMachine.getRoleIndices().get(getRole());
        start = System.currentTimeMillis();
        finishBy = timeout - 1000;

        // Get the moves available
        List<Move> moves = theMachine.getLegalMoves(getCurrentState(), getRole());
        Move selection = moves.get(0);
        System.out.println(selection);

        // If more than one move available choose best move, otherwise play only legal move
        if (moves.size() > 1) {
            selection = new UCT(finishBy, roleIndex, theMachine).UCTSearch(getCurrentState());
        }

        long stop = System.currentTimeMillis();
        notifyObservers(new GamerSelectedMoveEvent(moves, selection, stop - start));
        return selection;
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
