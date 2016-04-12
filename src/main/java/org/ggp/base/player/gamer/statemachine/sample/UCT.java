package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;


public class UCT extends MonteCarloTreeSearch {

    private final double EXPLORATION_CONSTANT = 1 / Math.sqrt(2);
    private final boolean MINIMAX = false;
    private Node rootNode;

    public UCT(long finishBy, Role role, StateMachine theMachine) {
        super(finishBy, role, theMachine);
    }

    public Move UCTSearch(MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
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
        return bestChild(rootNode, 0.0, MINIMAX).move;
    }

    public Node treePolicy(Node node) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        while (!theMachine.isTerminal(node.state)) {
            if(node.untriedMoves.size() != 0) {
                return expand(node);
            } else {
                node = bestChild(node, EXPLORATION_CONSTANT, MINIMAX);
            }
        }
        return node;
    }

    public Node bestChildMinimax(Node node, double explorationConstant) {
        Node bestChild = null;
        double bestScore;
        boolean myTurn;
        if(node.move != null) {
            myTurn = node.move.toString() != new Move(GdlPool.getConstant("noop")).toString();
        } else {
            myTurn = false;
        }
        if (myTurn) {
            bestScore = 0;
        } else {
            bestScore = 1;
        }
        for (Node child : node.children) {
            double score;
            if(myTurn) {
                score = (child.reward / child.visits) + explorationConstant * (Math.sqrt((2*Math.log(node.visits))/child.visits));
            } else {
                score = (child.reward / child.visits) - explorationConstant * (Math.sqrt((2*Math.log(node.visits))/child.visits));
            }
            System.out.println(child.move + " VISITS: " + child.visits  + " SCORE: " + score + " LEVEL: " + child.level);
            if (myTurn && score > bestScore || !myTurn && score < bestScore) {
                bestChild = child;
                bestScore = score;
            }
        }
        return bestChild;
    }

    public Node bestChildGen(Node node, double explorationConstant) {
        Node bestChild = null;
        double bestScore = 0.0;
        for (Node child : node.children) {
            double score = (child.reward / child.visits) + explorationConstant * (Math.sqrt((2*Math.log(node.visits))/child.visits));
            if(explorationConstant == 0.0)
                System.out.println(child.move + " VISITS: " + child.visits  + " SCORE: " + score);
            if (score > bestScore) {
                bestChild = child;
                bestScore = score;
            }
        }
        return bestChild;
    }

    public Node bestChild(Node node, double explorationConstant, boolean minimax) throws GoalDefinitionException {
        if(minimax) {
            return bestChildMinimax(node, explorationConstant);
        } else {
            return bestChildGen(node, explorationConstant);
        }
    }
}
