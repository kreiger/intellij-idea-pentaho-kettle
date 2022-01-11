package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.diagram.DiagramColors;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.DeleteProvider;
import com.intellij.openapi.graph.builder.EdgeCreationPolicy;
import com.intellij.openapi.graph.builder.GraphPresentationModel;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.view.*;
import com.intellij.util.MathUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import static javax.swing.SwingUtilities.isLeftMouseButton;

public class TransformationGraphPresentationModel extends GraphPresentationModel<Node, Edge> {
    private static final Logger LOGGER = Logger.getInstance(TransformationGraphPresentationModel.class);
    public static final String PENTAHO_GRAPH = "PentahoGraph";
    public static final Color EDGE_COLOR = DiagramColors.DEFAULT_EDGE.getDefaultColor();
    public final GraphSettings graphSettings;
    final PdiFacet facet;
    private final GraphManager graphManager;

    public TransformationGraphPresentationModel(GraphManager graphManager, GraphSettings graphSettings, PdiFacet facet) {
        this.graphManager = graphManager;
        this.graphSettings = graphSettings;
        this.facet = facet;
    }

    @Override public @NotNull NodeRealizer getNodeRealizer(@Nullable Node node) {
        NodeRealizer realizer;
        try {
            realizer = node.getRealizer(this);
        } catch (Exception e) {
            ImageNodeRealizer imageNodeRealizer = graphManager.createImageNodeRealizer();
            imageNodeRealizer.setImage(ImageUtil.MISSING_ENTRY_IMAGE);
            realizer = imageNodeRealizer;
            try {
                realizer.setWidth(node.getWidth());
                realizer.setHeight(node.getHeight());
                realizer.setX(node.getX());
                realizer.setY(node.getY());
            } catch (Exception e2) {
                LOGGER.warn(e2);
            }
        }
        return realizer;
    }

    @Override public @NotNull EdgeRealizer getEdgeRealizer(@Nullable Edge edge) {
        GenericEdgeRealizer edgeRealizer = graphManager.createGenericEdgeRealizer();
        edgeRealizer.setLabelText(null);
        edgeRealizer.setLineColor(EDGE_COLOR);
        //Arrow arrow = Arrow.Statics.addCustomArrow("Test", Arrow.STANDARD.getShape(), color);
        edgeRealizer.setArrow(Arrow.STANDARD);
        return edgeRealizer;
    }

    @Override public @Nullable NodeCellEditor getCustomNodeCellEditor(@Nullable Node step) {
        System.out.println("getCustomNodeCellEditor");
        return null;
    }

    @Override public @Nullable String getNodeTooltip(@Nullable Node node) {
        return node == null ? null : node.getTooltip();
    }

    @Override public @Nullable String getEdgeTooltip(@Nullable Edge edge) {
        return edge == null ? null : edge.toString();
    }

    @Override public boolean editNode(@Nullable Node node) {
        System.out.println("editNode: " + node);
        return true;
    }

    @Override public boolean editEdge(@Nullable Edge edge) {
        System.out.println("editEdge: " + edge);
        return false;
    }

    @Override public @NotNull DefaultActionGroup getNodeActionGroup(@Nullable Node node) {
        return node.getActionGroup();
    }

    @Override public @NotNull DefaultActionGroup getEdgeActionGroup(@Nullable Edge edge) {
        return edge.getActionGroup();
    }

    @Override public @NotNull DefaultActionGroup getPaperActionGroup() {
        return (DefaultActionGroup) ActionManager.getInstance().getAction("PentahoKettleTransformationPopupGroup");
    }

    @Override public @NotNull Layouter getDefaultLayouter() {
        return null;
    }

    @Override
    public EdgeLabel @NotNull [] getEdgeLabels(@Nullable Edge edge, @NotNull String s) {
        return new EdgeLabel[]{};
    }

    @Override public @NotNull GraphSettings getSettings() {
        return graphSettings;
    }

    @Override public void customizeSettings(@NotNull Graph2DView graph2DView, @NotNull EditMode editMode) {
        graph2DView.addViewChangeListener(view -> {
            System.out.println("view changed " + view);
        });
        editMode.allowBendCreation(false);
        editMode.allowEdgeCreation(false);
        editMode.allowMoveSelection(false);
        editMode.allowLabelSelection(false);
        graph2DView.setGridColor(Color.GRAY);
        graph2DView.setGridVisible(true);

        JComponent component = graph2DView.getCanvasComponent();
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            private Point2D point;
            private double x;
            private double y;

            @Override public void mousePressed(MouseEvent e) {
                if (isLeftMouseButton(e)) return;
                this.x = e.getX();
                this.y = e.getY();
                this.point = graph2DView.getViewPoint2D();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (isLeftMouseButton(e)) return;
                double x = e.getX();
                double y = e.getY();
                double diffX = (this.x - x);
                double diffY = (this.y - y);
                double zoom = graph2DView.getZoom();
                graph2DView.setViewPoint2D(point.getX() + diffX / zoom, point.getY() + diffY / zoom);
                graph2DView.updateView();
            }

            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                int units = e.getUnitsToScroll();
                double zoom = graph2DView.getZoom();
                double newZoom = zoom - units * 0.05d;
                double clamped = MathUtil.clamp(newZoom, 0.25D, 5.0D);
                graph2DView.setZoom(clamped);
                graph2DView.updateView();
            }
        };

        component.addMouseListener(mouseInputAdapter);
        component.addMouseMotionListener(mouseInputAdapter);
        component.addMouseWheelListener(mouseInputAdapter);
    }

    @Override public @NotNull EdgeCreationPolicy<Node> getEdgeCreationPolicy() {
        return (EdgeCreationPolicy<Node>) EdgeCreationPolicy.NOTHING_ACCEPTED_POLICY;
    }

    @Override public @Nullable DeleteProvider<?, ?> getDeleteProvider() {
        return null;
    }

    @Override public @NotNull String getActionPlace() {
        return PENTAHO_GRAPH;
    }

    @Override public void dispose() {
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }
}
