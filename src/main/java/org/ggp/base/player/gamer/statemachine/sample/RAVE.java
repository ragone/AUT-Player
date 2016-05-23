package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO: Description of RAVE.
 *
 * @author ragone.
 * @version 20/05/16
 */
public class RAVE extends UCT {
    public HashMap<Role, Set<Move>> usedMoves;

    public RAVE(long finishBy, Role role, StateMachine theMachine) {
        super(finishBy, role, theMachine);
        usedMoves = new HashMap<>();
        usedMoves.put(role, new HashSet<Move>());
        Role opponentRole = theMachine.getRoles().get(theMachine.getRoleIndices().get(role) == 1?0:1);
        usedMoves.put(opponentRole, new HashSet<Move>());
    }

    public Move RAVESearch(MachineState state) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
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
        return bestChildMinimax(rootNode, 0.0).move;
    }

    public Node treePolicy(Node node) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        while (!theMachine.isTerminal(node.state)) {
            if(node.nextStatesMap.size() != 0) {
                return expand(node);
            } else {
                node = bestChildMinimax(node, EXPLORATION_CONSTANT);
            }
        }
        return node;
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

    public Node bestChildMinimax(Node node, double explorationConstant) {
        Node bestChild = null;
        double bestScore = Double.POSITIVE_INFINITY;
//        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        System.out.println("-----------------------");
        for (Node child : node.children) {
            double score;
            double UCTScore;
            if(child.turn != node.myRole) {
                UCTScore = (child.reward / child.visits) + explorationConstant * (Math.sqrt((2*Math.log(node.visits))/child.visits));
            } else {
                UCTScore = (child.reward / child.visits) - explorationConstant * (Math.sqrt((2*Math.log(node.visits))/child.visits));
            }
            if(child.AMAFvisits > 0) {
                score = child.alphaValue * (child.AMAFreward / child.AMAFvisits) + (1 - child.alphaValue) * UCTScore;
            }  else {
                score = UCTScore;
            }

            System.out.println(child.move + " level: " + child.level + " score: " + score);
            System.out.println("\t visits: " + child.visits  + " reward: " + child.reward);
            System.out.println("\t amaf-visits: " + child.AMAFvisits  + " amaf-reward: " + child.AMAFreward);

            if(bestScore == Double.POSITIVE_INFINITY) {
                bestChild = child;
                bestScore = score;
            } else if (child.turn != node.myRole && score > bestScore || child.turn == node.myRole && score < bestScore) {
                bestChild = child;
                bestScore = score;
            }
        }
        if(bestChild == null) {
            System.out.print("");
        }
        System.out.println("-----------------------");
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return bestChild;
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
}
