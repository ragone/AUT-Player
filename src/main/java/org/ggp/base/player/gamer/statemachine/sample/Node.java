package org.ggp.base.player.gamer.statemachine.sample;

import java.util.List;

/**
 * TODO: Description of UCTNode.
 *
 * @author ragone.
 * @version 21/03/16
 */
public abstract class Node {
    protected int action;
    protected int visits;  // number of times visited
    protected float reward;  // accumulated reward value
    protected Node parent;  // null if root
    protected List<Node> children;

    public abstract void update(int value);    // update node and back propagate to parent
    public abstract void addChild(Node child); // add child node

    public Node(Node parent) {
        this.parent = parent;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }
}
