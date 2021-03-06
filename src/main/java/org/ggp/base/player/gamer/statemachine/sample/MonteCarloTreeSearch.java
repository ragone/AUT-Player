package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearch {

    public StateMachine theMachine;
    public Random random;
    public long finishBy;
    public Role role;
    public Node rootNode;
    public int playouts = 0;


    public MonteCarloTreeSearch(long finishBy, Role role, StateMachine theMachine) {
        this.finishBy = finishBy;
        this.role = role;
        this.theMachine = theMachine;
        random = new Random();
    }

    public Move MCSearch(MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
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
//        System.out.println(" TOTAL PLAYOUTS: " + playouts);
        return bestChild(rootNode).move;
    }

    public void backup(Node node, double reward) {
        while (node != null) {
            node.update(reward);
            node = node.parent;
        }
    }

    public double defaultPolicy(MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {
        while (!theMachine.isTerminal(state)) {
            List<Move> moves = theMachine.getRandomJointMove(state);
            state = theMachine.getNextState(state, moves);
        }
        playouts++;
        return theMachine.getGoal(state, role) / 100;
    }

    public Node treePolicy(Node node) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        while (!theMachine.isTerminal(node.state)) {
            if(node.nextStatesMap.size() != 0) {
                return expand(node);
            } else {
                node = bestChild(node);
            }
        }
        return node;
    }

    public Node bestChild(Node node) throws GoalDefinitionException {
        Node bestChild = null;
        double bestScore = 0;
        for (Node child : node.children) {
            double score = (child.reward / child.visits);
//            System.out.println(child.move + " VISITS: " + child.visits  + " SCORE: " + score + " LEVEL: " + child.level);

            if (score > bestScore) {
                bestChild = child;
                bestScore = score;
            }
        }
        return bestChild;
    }

    public Node expand(Node node) throws MoveDefinitionException, TransitionDefinitionException {
        Move move;
        if(node.turn == node.myRole) {
            move = node.myMoves.get(random.nextInt(node.myMoves.size()));
        } else {
            move = node.opponentMoves.get(random.nextInt(node.opponentMoves.size()));
        }
        List<MachineState> states = node.nextStatesMap.get(move);
        int statesIndex = random.nextInt(states.size());
        MachineState newState = states.get(statesIndex);
        states.remove(statesIndex);
        if(states.size() == 0) {
            node.nextStatesMap.remove(move);
            if(node.turn == node.myRole) {
                node.myMoves.remove(move);
            } else {
                node.opponentMoves.remove(move);
            }
        }
        Node child = node.addChild(newState, move);
        return child;
    }

    public int getIndex(Role role) {
        return theMachine.getRoleIndices().get(role);
    }
}
