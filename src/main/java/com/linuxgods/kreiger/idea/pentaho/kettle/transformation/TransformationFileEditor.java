package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeAnyChangeAbstractAdapter;
import com.intellij.psi.xml.XmlElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.events.DomEvent;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Arrow;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Notepad;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.ArrowComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.GoToStepListener;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.NodeComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.NotepadComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.JobFileEditor;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Hop;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
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

public class TransformationFileEditor extends UserDataHolderBase implements FileEditor {
    public static final Logger LOGGER = LoggerFactory.getLogger(TransformationFileEditor.class);
    private final GraphComponent graphComponent;
    private final VirtualFile file;
    private final Project project;

    public TransformationFileEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
        this.graphComponent = new GraphComponent(file);

        DomManager manager = DomManager.getDomManager(project);
        manager.addDomEventListener((@NotNull DomEvent event) -> {
            XmlElement xmlElement = event.getElement().getXmlElement();
            if (xmlElement != null && file.equals((xmlElement.getContainingFile().getVirtualFile()))) {
                update();
            }
        }, this);
        update();
        DumbService dumbService = DumbService.getInstance(project);
        if (dumbService.isDumb()) {
            dumbService.runWhenSmart(this::update);
        }
    }

    private void update() {
        Transformation.getTransformation(project, file).ifPresent(transformation -> {
            JComponent graphViewComponent = graphComponent.getView();
            graphViewComponent.removeAll();
            graphComponent.repaint();
            createGraph(graphViewComponent, transformation);
            graphComponent.revalidate();
            graphComponent.repaint();
        });
    }

    private void createGraph(JComponent graphViewComponent, Transformation transformation) {
        Map<Step, NodeComponent<Step>> stepComponents = transformation.getSteps().stream()
                .collect(toMap(identity(), this::createStepComponent));
        for (NodeComponent<Step> nodeComponent : stepComponents.values()) {
            nodeComponent.addMouseListener(new GoToStepListener(() -> {
                ((Navigatable) nodeComponent.getNode().getValue().getXmlTag()).navigate(true);
            }));
            graphViewComponent.add(nodeComponent);

        }
        for (Hop hop : transformation.getOrder().getHops()) {
            Step from = hop.getFrom().getValue();
            Step to = hop.getTo().getValue();
            if (from != null && to != null) {
                graphViewComponent.add(new ArrowComponent<>(stepComponents.get(from), stepComponents.get(to), new Arrow() {
                    @Override public Color getColor() {
                        return Arrow.DEFAULT_COLOR;
                    }

                    @Override public Optional<Icon> getIcon() {
                        return Optional.empty();
                    }
                }));
            }
        }
        for (Notepad notepad : transformation.getNotepads().getNotepads()) {
            try {
                graphViewComponent.add(new NotepadComponent(notepad));
            } catch (NullPointerException e) {
                LOGGER.warn("", e);
            }
        }

    }


    @NotNull private NodeComponent<Step> createStepComponent(Step step) {
        return new NodeComponent<>(new StepNode(step));
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

    private static class GraphComponent extends JPanel implements DataProvider, @NotNull Disposable {

        private final JPanel view;
        private final VirtualFile file;

        public GraphComponent(VirtualFile file) {
            super(new BorderLayout());
            this.file = file;
            view = new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                    super.paintComponent(g);
                }

                @Override
                public Dimension getPreferredSize() {
                    Component[] components = getComponents();
                    if (components.length == 0) return new Dimension(0, 0);
                    int minX = Integer.MAX_VALUE;
                    int minY = Integer.MAX_VALUE;
                    int maxWidth = 0;
                    int maxHeight = 0;
                    for (Component component : components) {
                        Dimension componentPreferredSize = component.getPreferredSize();
                        minX = Integer.min(minX, component.getX());
                        minY = Integer.min(minY, component.getY());
                        maxWidth = Integer.max(maxWidth, component.getX() + (int) componentPreferredSize.getWidth());
                        maxHeight = Integer.max(maxHeight, component.getY() + (int) componentPreferredSize.getHeight());
                    }
                    int margin = Math.max(minX, minY);
                    return new Dimension(maxWidth+margin, maxHeight+margin);
                }
            };
            DefaultActionGroup toolbarGroup = (DefaultActionGroup) ActionManager.getInstance().getAction("PentahoKettleTransformationGraphToolbar");
            ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("PentahoGraph", toolbarGroup, true);
            actionToolbar.setTargetComponent(view);
            add(actionToolbar.getComponent(), BorderLayout.NORTH);

            //var layer = new JLayer<>(view, new GraphLayerUI());
            JScrollPane scrollPane = new JBScrollPane(view);
            add(scrollPane, BorderLayout.CENTER);
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
