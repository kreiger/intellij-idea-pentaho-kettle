package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Hop;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components.ArrowComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components.GoToStepListener;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components.MouseDragAdapter;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components.StepComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Optional;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class TransformationFileEditor implements FileEditor {
    public static final Logger LOGGER = LoggerFactory.getLogger(TransformationFileEditor.class);
    private final GraphComponent graphComponent;
    private final VirtualFile file;

    public TransformationFileEditor(Project project, @NotNull VirtualFile file, PdiFacet pdiFacet) {
        this.file = file;
        this.graphComponent = new GraphComponent(file);
        JComponent graphViewComponent = graphComponent.getView();
        Transformation transformation = Transformation.getTransformation(project, file);
        createGraph(project, file, graphViewComponent, transformation, pdiFacet);

        transformation.getManager().addDomEventListener(event -> {
                LOGGER.warn("Document changed");
                graphViewComponent.removeAll();
                graphComponent.repaint();;
                createGraph(project, file, graphViewComponent, transformation, pdiFacet);
                graphComponent.revalidate();
                graphComponent.repaint();
            }, this);
    }

    private void createGraph(Project project, @NotNull VirtualFile file, JComponent graphViewComponent, Transformation transformation, PdiFacet pdiFacet) {
        Map<Step, StepComponent> stepComponents = transformation.getSteps().stream()
                .collect(toMap(identity(), step -> createStepComponent(pdiFacet, step)));
        for (StepComponent stepComponent : stepComponents.values()) {
            stepComponent.addMouseListener(new GoToStepListener(() -> {
                Step step = stepComponent.getStep();
                step.getTextOffset().ifPresent(textOffset -> {
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    fileEditorManager.openTextEditor(new OpenFileDescriptor(project, file, textOffset), true);
                });
            }));
            graphViewComponent.add(stepComponent);

        }
        for (Hop hop : transformation.getOrder().getHops()) {
            Step from = hop.getFrom().getValue();
            Step to = hop.getTo().getValue();
            if (from != null && to != null) {
                graphViewComponent.add(new ArrowComponent(stepComponents.get(from), stepComponents.get(to)));
            }
        }

    }


    @NotNull private StepComponent createStepComponent(PdiFacet facet, Step step) {
        String type = step.getType().getValue();
        Icon icon = facet.getIcon(type).orElse(ImageUtil.MISSING_ENTRY_ICON);
        return new StepComponent(step, icon);
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

    private static class GraphComponent extends JPanel implements DataProvider, @NotNull Disposable {

        private final JComponent view;
        private final VirtualFile file;

        public GraphComponent(VirtualFile file) {
            super(new BorderLayout());
            this.file = file;
            view = new JComponent() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                    super.paintComponent(g);
                }
            };
            DefaultActionGroup toolbarGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("PentahoKettleTransformationGraphToolbar");
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("PentahoGraph", toolbarGroup, true);
            actionToolbar.setTargetComponent(view);
            add(actionToolbar.getComponent(), BorderLayout.NORTH);

            new MouseDragAdapter(view) {
                @Override public void mouseDraggedTo(Component component, int x, int y) {
                    view.setLocation(x,y);
                    view.repaint();
                }
            };

            //var layer = new JLayer<>(view, new GraphLayerUI());
            add(new JScrollPane(view), BorderLayout.CENTER);
        }

        public JComponent getView() {
            return view;
        }

        @Override public @Nullable Object getData(@NotNull @NonNls String dataId) {
            if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
                return this.file;
            }
            return null;
        }

        @Override public void dispose() {
            getParent().remove(this);
        }
    }

}
