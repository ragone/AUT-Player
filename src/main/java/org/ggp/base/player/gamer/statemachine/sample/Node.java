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
    public final int RAVE_CUTOFF = 100;

    public final Role role;
    public Role opponentRole;
    public boolean myTurn;
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

    public Node(MachineState state, Node parent, Move move, int level, StateMachine theMachine, Role role) throws MoveDefinitionException, TransitionDefinitionException {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.theMachine = theMachine;
        this.role = role;
        children = new ArrayList<>();
        opponentRole = theMachine.getRoles().get(theMachine.getRoleIndices().get(role) == 1?0:1);
        if(theMachine.isTerminal(state)) {
            myMoves = new ArrayList<>();
            opponentMoves = new ArrayList<>();
            nextStatesMap = new HashMap<>();
        } else {
            myMoves = new ArrayList<>(theMachine.getLegalMoves(state, role));
            opponentMoves = theMachine.getLegalMoves(state, opponentRole);
            nextStatesMap = theMachine.getNextStates(state, role);
        }
        visits = 0;
        reward = 0.0;
        AMAFreward = 0.0;
        AMAFvisits = 0;
        alphaValue = 1.0;

        this.level = level;
        if(myMoves.size() == 1) {
            // Not my turn
            myTurn = false;
        } else {
            myTurn = true;
        }
    }

    public void update(double value) {
        visits++;
        reward += value;
    }

    public void updateAMAF(double value) {
        visits++;
        reward += value;
        AMAFvisits++;
        AMAFreward += value;
        updateAlphaValue();
    }

    public Node addChild(MachineState state, Move move) throws MoveDefinitionException, TransitionDefinitionException {
        Node child;
        child = new Node(state, this, move, level + 1, theMachine, role);
        children.add(child);
        return child;
    }

    public void updateAlphaValue() {
        double newAlpha = (RAVE_CUTOFF - visits)/RAVE_CUTOFF;
        if (newAlpha < 0.0) {
            newAlpha = 0.0;
        }
        alphaValue = newAlpha;

    }

}