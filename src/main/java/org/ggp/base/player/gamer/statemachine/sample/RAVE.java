package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

/**
 * TODO: Description of RAVE.
 *
 * @author ragone.
 * @version 20/05/16
 */
public class RAVE extends MonteCarloTreeSearch {
    public RAVE(long finishBy, Role role, StateMachine theMachine) {
        super(finishBy, role, theMachine);

    }

    public Move RAVESearch(MachineState state) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        // Set the root node parent to null
        rootNode = new Node(state, null, null, 0, theMachine, role);
        // Until time runs out, run algorithm
        Node currentNode = rootNode;
        while(System.currentTimeMillis() < finishBy) {
            currentNode = treePolicy(rootNode);
            double reward = defaultPolicy(currentNode.state);
            backup(currentNode, reward);
        }
        // Return best child move
        return bestChild(rootNode).move;
    }
}
