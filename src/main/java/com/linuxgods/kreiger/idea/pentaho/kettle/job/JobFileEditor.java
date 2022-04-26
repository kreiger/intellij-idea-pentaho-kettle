package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.diff.util.FileEditorBase;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.KettleTextEditorWithPreview;
import com.intellij.openapi.fileEditor.TextEditorWithPreview;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlElement;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.xml.DomManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Arrow;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Notepad;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.ArrowComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.GoToStepListener;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.NodeComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.components.NotepadComponent;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.dom.Entry;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.dom.Hop;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.dom.Job;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Optional;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class JobFileEditor extends FileEditorBase {
    public static final Logger LOGGER = LoggerFactory.getLogger(JobFileEditor.class);
    private final GraphComponent graphComponent;
    private final JComponent component;
    private final Project project;
    private final VirtualFile file;

    public JobFileEditor(Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
        this.graphComponent = new GraphComponent(file);
        DumbService dumbService = DumbService.getInstance(project);
        this.component = dumbService.wrapWithSpoiler(graphComponent, this::update, this);

        DomManager manager = DomManager.getDomManager(project);
        manager.addDomEventListener(event -> {
            XmlElement xmlElement = event.getElement().getXmlElement();
            if (xmlElement != null && file.equals((xmlElement.getContainingFile().getVirtualFile()))) {
                update();
            }
        }, this);
        update();
    }

    @Override
    public void selectNotify() {
        System.out.println("selectNotify");
        update();
    }

    private void update() {
        Job.getJob(project, file).ifPresent(job -> {
            JComponent graphViewComponent = graphComponent.getView();
            graphViewComponent.removeAll();
            graphComponent.repaint();
            createGraph(graphViewComponent, job);
            graphComponent.revalidate();
            graphComponent.repaint();
        });
    }

    private void createGraph(JComponent graphViewComponent, Job job) {
        Map<Entry, NodeComponent<Entry>> entryComponents = job.getEntries().getEntries().stream()
                .filter(Entry::isDraw)
                .collect(toMap(identity(), this::createEntryComponent));
        for (NodeComponent<Entry> nodeComponent : entryComponents.values()) {
            Navigatable xmlTag = (Navigatable) nodeComponent.getNode().getValue().getXmlTag();
            if (xmlTag != null) {
                nodeComponent.addMouseListener(new GoToStepListener(() -> {
                    KettleTextEditorWithPreview editor = (KettleTextEditorWithPreview) FileEditorManagerEx.getInstanceEx(
                            project).getSelectedEditor(file);
                    if (editor.getLayout() == TextEditorWithPreview.Layout.SHOW_PREVIEW) {
                        editor.setLayout(TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW);
                    }
                    xmlTag.navigate(true);
                }));
            }
            graphViewComponent.add(nodeComponent);

        }
        for (Hop hop : job.getHops().getHops()) {
            Entry from = hop.getFrom().getValue();
            Entry to = hop.getTo().getValue();
            if (from != null && to != null) {
                graphViewComponent.add(
                        new ArrowComponent(entryComponents.get(from), entryComponents.get(to), new Arrow() {
                            @Override
                            public Color getColor() {
                                return hop.getColor();
                            }

                            @Override
                            public Optional<Icon> getIcon() {
                                return Optional.ofNullable(hop.getIcon());
                            }
                        }));
            }
        }

        for (Notepad notepad : job.getNotepads().getNotepads()) {
            try {
                graphViewComponent.add(new NotepadComponent(notepad));
            } catch (NullPointerException e) {
                LOGGER.warn("", e);
            }
        }

    }


    @NotNull
    private NodeComponent<Entry> createEntryComponent(Entry entry) {
        return new NodeComponent<>(new EntryNode(entry));
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return file;
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return graphComponent;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return "Graph";
    }

    private static class GraphComponent extends JPanel implements DataProvider, @NotNull Disposable {

        private final JPanel view;
        private final VirtualFile file;

        public GraphComponent(VirtualFile file) {
            super(new BorderLayout());
            this.file = file;
            view = new JPanel(null) {
                @Override
                protected void paintComponent(Graphics g) {
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
                    return new Dimension(maxWidth + margin, maxHeight + margin);
                }
            };
            DefaultActionGroup toolbarGroup = (DefaultActionGroup) ActionManager.getInstance()
                    .getAction("PentahoKettleTransformationGraphToolbar");
            ActionToolbar actionToolbar = ActionManager.getInstance()
                    .createActionToolbar("PentahoGraph", toolbarGroup, true);
            actionToolbar.setTargetComponent(view);
            add(actionToolbar.getComponent(), BorderLayout.NORTH);

            //var layer = new JLayer<>(view, new GraphLayerUI());
            JScrollPane scrollPane = new JBScrollPane(view);
            add(scrollPane, BorderLayout.CENTER);
        }

        public JComponent getView() {
            return view;
        }

        @Override
        public @Nullable Object getData(@NotNull @NonNls String dataId) {
            if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
                return this.file;
            }
            return null;
        }

        @Override
        public void dispose() {
            getParent().remove(this);
        }
    }

}
