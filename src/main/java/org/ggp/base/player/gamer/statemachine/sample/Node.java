package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    private final StateMachine theMachine;
    public final int RAVE_CUTOFF = 1000;

    public final Role myRole;
    public final Role opponentRole;
    public Role turn;
    public int level;
    public Move move;
    public int visits;  // number of times visited
    public int AMAFvisits;
    public double AMAFreward;
    public double reward;  // accumulated reward value
    public Node parent;  // null if root
    public List<Node> children;
    public double alphaValue;
    public List<Move> myMoves;
    public MachineState state;
    public List<Move> opponentMoves;
    public Map<Move, List<MachineState>> nextStatesMap;

    public Node(MachineState state, Node parent, Move move, int level, StateMachine theMachine, Role myRole) throws MoveDefinitionException, TransitionDefinitionException {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.theMachine = theMachine;
        this.myRole = myRole;
        children = new ArrayList<>();
        opponentRole = theMachine.getRoles().get(theMachine.getRoleIndices().get(myRole) == 1?0:1);
        if(parent == null || parent.turn == opponentRole) {
            turn = myRole;
        } else {
            turn = opponentRole;
        }
        if(theMachine.isTerminal(state)) {
            myMoves = new ArrayList<>();
            opponentMoves = new ArrayList<>();
            nextStatesMap = new HashMap<>();
        } else {
            myMoves = new ArrayList<>(theMachine.getLegalMoves(state, myRole));
            opponentMoves = new ArrayList<>(theMachine.getLegalMoves(state, opponentRole));
            if (turn == myRole) {
                nextStatesMap = theMachine.getNextStates(state, myRole);
            } else {
                nextStatesMap = theMachine.getNextStates(state, opponentRole);
            }
        }
        visits = 0;
        reward = 0.0;
        AMAFreward = 0.0;
        AMAFvisits = 0;
        alphaValue = 1.0;

        this.level = level;
    }

    public void update(double value) {
        visits++;
        reward += value;
    }

    public void updateAMAF(double value) {
        AMAFvisits++;
        AMAFreward += value;
        updateAlphaValue();
    }

    public Node addChild(MachineState state, Move move) throws MoveDefinitionException, TransitionDefinitionException {
        Node child = new Node(state, this, move, level + 1, theMachine, myRole);
        children.add(child);
        return child;
    }

    public void updateAlphaValue() {
        double newAlpha = (RAVE_CUTOFF - AMAFvisits)/(double)RAVE_CUTOFF;
        if (newAlpha < 0.0) {
            newAlpha = 0.0;
        }
        alphaValue = newAlpha;

    }
}