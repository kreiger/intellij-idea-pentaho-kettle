package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.graph.builder.GraphDataModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class TransformationGraphDataModel extends GraphDataModel<Node, Edge> {
    private List<Edge> edges;
    private Map<String, Node> nodeMap;

    public TransformationGraphDataModel(Project project, VirtualFile file) {
        init(project, file);
    }

    public void init(Project project, VirtualFile file) {
        PsiManager psiManager = PsiManager.getInstance(project);
        XmlFile xmlFile = (XmlFile) psiManager.findFile(file);
        DomManager domManager = DomManager.getDomManager(project);
        DomFileElement<Transformation> fileElement = domManager.getFileElement(xmlFile, Transformation.class);
        Transformation transformation = fileElement.getRootElement();
        Stream<StepNode> stepNodes = transformation.getSteps().stream().map(StepNode::new);
        Stream<NotepadNode> notepadNodes = transformation.getNotepads().getNotepads().stream().map(NotepadNode::new);
        List<Node> nodes = Stream.concat(stepNodes, notepadNodes).collect(toList());
        List<Edge> edges = transformation.getOrder().getHops().stream().map(HopEdge::new).collect(toList());
        init(nodes, edges);
    }

    private void init(List<Node> nodes, List<Edge> edges) {
        this.nodeMap = nodeMap(nodes);
        this.edges = edges;
    }

    @NotNull private Map<String, Node> nodeMap(List<Node> nodes) {
        return nodes.stream().collect(toMap(Node::getName, identity()));
    }

    @Override public void dispose() {

    }

    @Override public @NotNull Collection<Node> getNodes() {
        return nodeMap.values();
    }

    @Override public @NotNull Collection<Edge> getEdges() {
        return edges;
    }

    @NotNull @Override public Node getSourceNode(Edge edge) {
        return nodeMap.get(edge.getFrom());
    }

    @NotNull @Override public Node getTargetNode(Edge edge) {
        return nodeMap.get(edge.getTo());
    }

    @Override public @NotNull String getNodeName(Node node) {
        return node.getName();
    }

    @Override public @NotNull String getEdgeName(Edge edge) {
        return edge.toString();
    }

    @Nullable
    @Override
    public Edge createEdge(@NotNull Node n1, @NotNull Node n2) {
        return null;
    }
}
