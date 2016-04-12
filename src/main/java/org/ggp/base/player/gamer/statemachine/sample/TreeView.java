package org.ggp.base.player.gamer.statemachine.sample;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;

/**
 * TODO: Description of TreeView.
 *
 * @author ragone.
 * @version 11/04/16
 */
public class TreeView extends JFrame {
    public TreeView() {
        setSize(800, 600);
        mxGraph graph = new mxGraph();

        graph.getModel().beginUpdate();
        Object v1 = graph.addCell(new Object());
        Object v2 = graph.addCell(new Object());
        graph.getModel().endUpdate();

        mxCompactTreeLayout layout = new mxCompactTreeLayout(graph);
        layout.execute(graph.getDefaultParent());
        mxGraphComponent graphUIComponent = new mxGraphComponent(graph);
        add(graphUIComponent);
        show();
    }
}
