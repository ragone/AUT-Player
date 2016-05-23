package org.ggp.base.player.gamer.statemachine.sample;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import javax.annotation.processing.SupportedAnnotationTypes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Description of AMAF.
 *
 * @author ragone.
 * @version 22/05/16
 */
public class AMAF extends MonteCarloTreeSearch {
    public HashMap<Role, Set<Move>> usedMoves;


    public AMAF(long finishBy, Role role, StateMachine theMachine) {
        super(finishBy, role, theMachine);
        usedMoves = new HashMap<>();
        usedMoves.put(role, new HashSet<Move>());
        Role opponentRole = theMachine.getRoles().get(theMachine.getRoleIndices().get(role) == 1?0:1);
        usedMoves.put(opponentRole, new HashSet<Move>());
    }

    public Move AMAFSearch(MachineState state) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        // Set the root node parent to null
        rootNode = new Node(state, null, null, 0, theMachine, role);
        // Until time runs out, run algorithm
        Node currentNode;
        while(System.currentTimeMillis() < finishBy) {
            currentNode = treePolicy(rootNode);
            double reward = defaultPolicy(currentNode.state);
            backup(currentNode, reward);
        }
        // Return best child move
        return bestChild(rootNode).move;
    }


    public void backup(Node node, double reward) {
        while (node != null) {
            node.update(reward);
            if(node.parent != null) {
                for (Node sibling : node.parent.children) {
                    if(usedMoves.get(sibling.turn).contains(sibling.move)) {
                        sibling.updateAMAF(reward);
                    }
                }
            }
            node = node.parent;
        }
    }

    public double defaultPolicy(MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {
        while (!theMachine.isTerminal(state)) {
            List<Move> moves = theMachine.getRandomJointMove(state);
            state = theMachine.getNextState(state, moves);
            usedMoves.get(rootNode.myRole).add(moves.get(getIndex(rootNode.myRole)));
            usedMoves.get(rootNode.opponentRole).add(moves.get(getIndex(rootNode.opponentRole)));
        }
        return theMachine.getGoal(state, role) / 100;
    }
}
