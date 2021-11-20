package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.builder.GraphBuilderFactory;
import com.intellij.openapi.graph.layout.Layouter;
import com.intellij.openapi.graph.layout.seriesparallel.SeriesParallelLayouter;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class TransformationFileEditor implements FileEditor {
    private final JComponent graphComponent;
    private final VirtualFile file;

    public TransformationFileEditor(Project project, @NotNull VirtualFile file) {
        this.file = file;
        TransformationGraphDataModel graphDataModel = getTransformationGraphDataModel(project, file);
        var graphBuilderFactory = GraphBuilderFactory.getInstance(project);
        var graphManager = GraphManager.getGraphManager();
        GraphSettings graphSettings = new GraphSettings();
        graphSettings.setCurrentLayouter(Layouter.DUMB_UNAPPLICABLE);
        graphSettings.setFitContentAfterLayout(false);
        //graphSettings.setShowEdgeLabels(false);
        PdiFacet facet = PdiFacet.getInstance(project, file);
        var graphPresentationModel = new TransformationGraphPresentationModel(graphManager, graphSettings, facet);
        var graphBuilder = graphBuilderFactory.createGraphBuilder(graphDataModel, graphPresentationModel);
        this.graphComponent = new JPanel(new BorderLayout());
        DefaultActionGroup toolbarGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("PentahoKettleTransformationGraphToolbar");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("PentahoGraph", toolbarGroup, true);
        JComponent graphViewComponent = graphBuilder.getView().getJComponent();
        actionToolbar.setTargetComponent(graphViewComponent);
        graphComponent.add(actionToolbar.getComponent(), "North");
        graphComponent.add(graphViewComponent, "Center");
        graphBuilder.initialize();
        FileDocumentManager.getInstance().getDocument(file).addDocumentListener(new DocumentListener() {
            @Override public void documentChanged(@NotNull DocumentEvent event) {
                graphDataModel.init(project, file);
                graphBuilder.queueUpdate();
            }
        });
    }

    @NotNull
    private TransformationGraphDataModel getTransformationGraphDataModel(Project project, @NotNull VirtualFile file) {
        return new TransformationGraphDataModel(project, file);
    }

    @NotNull private SeriesParallelLayouter getSeriesParallelLayouter(GraphManager graphManager) {
        SeriesParallelLayouter seriesParallelLayouter = graphManager.createSeriesParallelLayouter();
        seriesParallelLayouter.setGeneralGraphHandlingEnabled(true);
        seriesParallelLayouter.setParallelEdgeLayouter(graphManager.createPolylineLayoutStage());
        return seriesParallelLayouter;
    }

    @Override public @Nullable VirtualFile getFile() {
        return file;
    }

    @Override public @NotNull JComponent getComponent() {
        return graphComponent;
    }

    @Override public @Nullable JComponent getPreferredFocusedComponent() {
        return graphComponent;
    }

    @Override public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Graph";
    }

    @Override public void setState(@NotNull FileEditorState state) {

    }

    @Override public boolean isModified() {
        return false;
    }

    @Override public boolean isValid() {
        return true;
    }

    @Override public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override public void dispose() {

    }

    @Override public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return null;
    }

    @Override public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {

    }

}
