package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Description of Node.
 *
 * @author ragone.
 * @version 2/04/16
 */
public class Node {
    private final StateMachine theMachine;
    public final Role role;
    public int level;
    public Move move;
    public int visits;  // number of times visited
    public double reward;  // accumulated reward value
    public Node parent;  // null if root
    public List<Node> children;
    public List<Move> untriedMoves;
    public MachineState state;

    public Node(MachineState state, Node parent, Move move, int level, StateMachine theMachine, Role role) throws MoveDefinitionException {
        this.state = state;
        this.parent = parent;
        this.move = move;
        this.theMachine = theMachine;
        this.role = role;
        children = new ArrayList<>();
        if(theMachine.isTerminal(state)) {
            untriedMoves = new ArrayList<>();
        } else {
            untriedMoves = new ArrayList<>(theMachine.getLegalMoves(state, role));
        }
        visits = 0;
        reward = 0.0;
        this.level = level;
    }

    public void update(double value) {
        visits++;
        reward += value;
    }

    public Node addChild(MachineState state, Move move) throws MoveDefinitionException {
        Node child;
        child = new Node(state, this, move, level + 1, theMachine, role);
        children.add(child);
        untriedMoves.remove(move);
        return child;
    }
}