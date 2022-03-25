package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.SequencePresentation;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.BlockInlayPriority;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Hop;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class StepInlayHintsProvider implements InlayHintsProvider<NoSettings> {


    private static final SettingsKey<NoSettings> SETTINGS_KEY = new SettingsKey<>("Step Hints");

    @NotNull
    @Override
    public PsiFile createFile(@NotNull Project project, @NotNull FileType fileType, @NotNull Document document) {
        return InlayHintsProvider.super.createFile(project, fileType, document);
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull NoSettings noSettings, @NotNull InlayHintsSink inlayHintsSink) {
        Transformation transformation = Transformation.getTransformation(psiFile.getProject(), psiFile.getVirtualFile());
        DomManager domManager = DomManager.getDomManager(psiFile.getProject());
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor1, @NotNull InlayHintsSink sink) {
                if (!(psiElement instanceof XmlTag)) {
                    return true;
                }
                XmlTag xmlTag = (XmlTag) psiElement;
                DomElement domElement = domManager.getDomElement(xmlTag);
                if (domElement == null) {
                    return true;
                }
                DomElement parent = domElement.getParent();
                if (!(parent instanceof Step) || !"name".equals(domElement.getXmlElementName())) {
                    return true;
                }
                Step step = (Step) parent;
                for (Hop hop : transformation.getOrder().getHops()) {
                    Step from = hop.getFrom().getValue();
                    Step to = hop.getTo().getValue();
                    if (null == from || null == to) {
                        continue;
                    }
                    InlayPresentationFactory.HoverListener hoverListener = new InlayPresentationFactory.HoverListener() {
                        private Runnable hoverFinished;

                        @Override
                        public void onHover(@NotNull MouseEvent mouseEvent, @NotNull Point point) {
                            Component component = mouseEvent.getComponent();
                            component.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            this.hoverFinished = () -> component.setCursor(null);
                        }

                        @Override
                        public void onHoverFinished() {
                            this.hoverFinished.run();
                        }
                    };
                    if (to.equals(step)) {
                        int offset = xmlTag.getTextOffset();
                        Document document = editor1.getDocument();
                        int line = document.getLineNumber(offset);
                        int startOffset = document.getLineStartOffset(line);
                        int column = offset - startOffset;
                        PresentationFactory factory = this.getFactory();
                        InlayPresentation text = factory.text(from.getType().getStringValue() + ": " + from.getNameUntrimmed() + " \u27F6");
                        InlayPresentation mouseHandling = factory.mouseHandling(text, (mouseEvent, point) -> ((NavigatablePsiElement) from.getXmlTag()).navigate(true), hoverListener);
                        InlayPresentation inlayPresentation = new SequencePresentation(java.util.List.of(factory.textSpacePlaceholder(column, false), mouseHandling));
                        sink.addBlockElement(offset, false, true, 0, inlayPresentation);
                    }
                    if (from.equals(step)) {
                        int offset = xmlTag.getTextOffset();
                        Document document = editor.getDocument();
                        int line = editor1.getDocument().getLineNumber(offset);
                        int startOffset = document.getLineStartOffset(line);
                        int column = offset - startOffset;
                        int nextLine = line + 1;
                        int nextLineStartOffset = document.getLineStartOffset(nextLine);
                        PresentationFactory factory = this.getFactory();
                        InlayPresentation text = factory.text("\u27F6 " + to.getType().getStringValue() + ": " + to.getNameUntrimmed());
                        InlayPresentation mouseHandling = factory.mouseHandling(text, (mouseEvent, point) -> ((NavigatablePsiElement) to.getXmlTag()).navigate(true), hoverListener);
                        InlayPresentation inlayPresentation = new SequencePresentation(java.util.List.of(factory.textSpacePlaceholder(column, false), mouseHandling));
                        sink.addBlockElement(nextLineStartOffset, true, true, 0, inlayPresentation);
                    }
                }
                return true;
            }

            @NotNull
            private InlayPresentation getIndentedPresentation(String text, int offset) {
                Document document = editor.getDocument();
                int line = document.getLineNumber(offset);
                int startOffset = document.getLineStartOffset(line);
                int column = offset - startOffset;
                PresentationFactory factory = this.getFactory();
                return new SequencePresentation(List.of(factory.textSpacePlaceholder(column, true), factory.smallText(text)));
            }
        };
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "Step";
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return SETTINGS_KEY;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return null;
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings noSettings) {
        return changeListener -> {
            JPanel panel = new JPanel();
            panel.setVisible(false);
            return panel;
        };
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return DefaultImpls.isLanguageSupported(this, language);
    }

    @Override
    public boolean isVisibleInSettings() {
        return false;
    }
}
