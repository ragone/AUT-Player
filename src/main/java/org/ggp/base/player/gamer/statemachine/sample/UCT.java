package org.ggp.base.player.gamer.statemachine.sample;

import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TODO: Description of UCT.
 *
 * @author ragone.
 * @version 27/03/16
 */
public class UCT {


    private final int SAMPLE_SIZE = 1;
    private final double EXPLORATION_CONSTANT = Math.sqrt(2);
    private StateMachine theMachine;
    private Random random;
    private long finishBy;
    private int roleIndex;
    private Node rootNode;

    public UCT(long finishBy, int roleIndex, StateMachine theMachine) {
        this.finishBy = finishBy;
        this.roleIndex = roleIndex;
        this.theMachine = theMachine;
        random = new Random();
    }

    /**
     * Returns best move based on MCTS with UCT.
     * @param state
     * @return move
     * @throws MoveDefinitionException
     * @throws TransitionDefinitionException
     * @throws GoalDefinitionException
     */
    public Move UCTSearch(MachineState state) throws MoveDefinitionException, TransitionDefinitionException, GoalDefinitionException {
        // Set the root node parent to null
        rootNode = new Node(state, null, null, 0);
        // Until time runs out, run algorithm
        while(System.currentTimeMillis() < finishBy) {
            Node currentNode = treePolicy(rootNode);
            for(int i = 0; i < SAMPLE_SIZE; i++) {
                double reward = defaultPolicy(currentNode.state);
                backup(currentNode, reward);
            }
        }
        // Return best child move
        return bestChild(rootNode, 0.0).move;
    }

    /**
     *
     * @param node
     * @param reward
     */
    private void backup(Node node, double reward) {
        while (node != null) {
            node.update(reward);
            node = node.parent;
        }
    }

    /**
     * Also known as roll out policy. Play randomly until terminal state and returns the score.
     * @param state
     * @return score of terminal state
     * @throws MoveDefinitionException
     * @throws GoalDefinitionException
     * @throws TransitionDefinitionException
     */
    private double defaultPolicy(MachineState state) throws MoveDefinitionException, GoalDefinitionException, TransitionDefinitionException {
        while (!theMachine.isTerminal(state)) {
            List<Move> moves = theMachine.getRandomJointMove(state);
            state = theMachine.getNextState(state, moves);
        }
        return theMachine.getGoal(state, theMachine.getRoles().get(roleIndex)) / 100;
    }

    private Node treePolicy(Node node) throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
        while (!theMachine.isTerminal(node.state)) {
            if(node.untriedMoves.size() != 0) {
                return expand(node);
            } else {
                node = bestChild(node, EXPLORATION_CONSTANT);
            }
        }
        return node;
    }

    private Node bestChild(Node node, double explorationConstant) throws GoalDefinitionException {
        Node bestChild = null;
        double bestScore = 0;
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

    private Node expand(Node node) throws MoveDefinitionException, TransitionDefinitionException {
        int randomIndex = random.nextInt(node.untriedMoves.size());
        Move move = node.untriedMoves.get(randomIndex);
        MachineState newState = theMachine.getRandomNextState(node.state, theMachine.getRoles().get(roleIndex), move);
        Node child = node.addChild(newState, move);
//        System.out.println("Node expanded: " + child.move + " LEVEL: " + child.level + " VISITS: " + child.visits);
        return child;
    }


    public class Node {
        private int level;
        private Move move;
        private int visits;  // number of times visited
        private double reward;  // accumulated reward value
        private Node parent;  // null if root
        private List<Node> children;
        private List<Move> untriedMoves;
        private MachineState state;

        public Node(MachineState state, Node parent, Move move, int level) throws MoveDefinitionException {
            this.state = state;
            this.parent = parent;
            this.move = move;
            children = new ArrayList<>();
            if(theMachine.isTerminal(state)) {
                untriedMoves = new ArrayList<>();
            } else {
                untriedMoves = new ArrayList<>(theMachine.getLegalMoves(state, theMachine.getRoles().get(roleIndex)));
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
            Node child = new Node(state, this, move, level + 1);
            children.add(child);
            untriedMoves.remove(move);
            return child;
        }
    }

}
