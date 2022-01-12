package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.codeInsight.daemon.*;
import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.codeInsight.hints.presentation.SequencePresentation;
import com.intellij.ide.IconProvider;
import com.intellij.ide.presentation.PresentationProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.BlockInlayPriority;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.paths.PathReference;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.util.xml.*;
import com.intellij.util.xml.converters.PathReferenceConverter;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationLanguage;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import static com.intellij.patterns.XmlPatterns.xmlText;
import static java.util.stream.Collectors.toList;

public interface Step extends DomElement {

    @NameValue
    default String getNameUntrimmed() {
        XmlTag xmlTag = getXmlTag();
        if (xmlTag == null) return null;
        String nameText = xmlTag.getSubTagText("name");
        if (nameText == null) return null;
        return StringEscapeUtils.unescapeXml(nameText);
    }

    @Required
    GenericDomValue<String> getName();

    @Required
    @Referencing(value = StepTypeConverter.class, soft = true)
    GenericDomValue<String> getType();

    @Required
    GUI getGUI();

    @Convert(PathReferenceConverter.class)
    GenericDomValue<PathReference> getFilename();

    default int getX() {
        return getGUI().getXloc().getValue();
    }

    default int getY() {
        return getGUI().getYloc().getValue();
    }

    default OptionalInt getTextOffset() {
        XmlTag xmlTag = getXmlTag();
        if (xmlTag == null) {
            return OptionalInt.empty();
        }
        XmlTag nameTag = xmlTag.findFirstSubTag("name");
        if (nameTag == null) {
            return OptionalInt.of(xmlTag.getTextOffset());
        }
        XmlTagValue nameText = nameTag.getValue();
        return OptionalInt.of(nameText.getTextRange().getStartOffset());
    }

    interface Type extends DomElement {
        String getValue();
    }

    class StepPresentationProvider extends PresentationProvider<Step> {
        @Override public @Nullable String getName(Step step) {
            return super.getName(step);
        }

        @Override public @Nullable Icon getIcon(Step step) {

            String type = step.getType().getStringValue();
            return Optional.ofNullable(step.getModule())
                    .flatMap(PdiFacet::getInstance)
                    .flatMap(pdiFacet -> pdiFacet.getIcon(type))
                    .orElse(null);
        }

        @Override public @Nullable @Nls(capitalization = Nls.Capitalization.Title) String getTypeName(Step step) {
            return "Step";
        }

    }

    class StepIconProvider extends IconProvider {
        @Override public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
            if (element instanceof XmlTag) {
                XmlTag xmlTag = (XmlTag) element;
                if ("type".equals(xmlTag.getName())) {
                    String type = xmlTag.getValue().getTrimmedText();
                    return PdiFacet.getInstance(element.getProject(), element.getContainingFile().getVirtualFile())
                            .flatMap(pdiFacet -> pdiFacet.getIcon(type))
                            .orElse(null);
                }
            }
            return null;
        }
    }

    class StepLineMarkerProvider extends LineMarkerProviderDescriptor {

        public static final PsiElementPattern.Capture<PsiElement> STEP_TYPE_PATTERN = psiElement().withElementType(XmlTokenType.XML_DATA_CHARACTERS).inside(xmlText()
                .withParent(xmlTag().withLocalName("type")
                        .withParent(xmlTag().withLocalName("step"))));

        @Override public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
            if (!STEP_TYPE_PATTERN.accepts(element)) return null;
            XmlText xmlText = (XmlText) element.getParent();
            String type = xmlText.getValue();
            return PdiFacet.getInstance(element)
                    .flatMap(pdiFacet -> pdiFacet.getIcon(type)
                            .map(icon -> getPsiElementLineMarkerInfo(element, type, pdiFacet, icon)))
                    .orElse(null);

        }

        @NotNull
        private LineMarkerInfo<PsiElement> getPsiElementLineMarkerInfo(PsiElement element, String type, PdiFacet pdiFacet, Icon icon) {
            List<NavigatablePsiElement> stepMetaClasses = pdiFacet.findStepMetaClasses(type, element.getResolveScope())
                    .flatMap(psiClass -> Stream.<NavigatablePsiElement>concat(Stream.of(psiClass), getPublicMethods(psiClass, "getXML", "loadXML")))
                    .collect(toList());
            LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(), icon, null, new DefaultGutterIconNavigationHandler<>(stepMetaClasses, type), GutterIconRenderer.Alignment.RIGHT, () -> type);
            return NavigateAction.setNavigateAction(lineMarkerInfo, "Go To " + type, "GotoClass", icon);
        }

        @NotNull
        private Stream<? extends PsiMethod> getPublicMethods(PsiClass psiClass, String... methodNames) {
            Set<String> methodNamesSet = Set.of(methodNames);
            return Arrays.stream(psiClass.getMethods())
                    .filter(psiMethod -> psiMethod.hasModifierProperty(PsiModifier.PUBLIC))
                    .filter(psiMethod -> methodNamesSet.contains(psiMethod.getName()));
        }

        @Override public @Nullable("null means disabled") @GutterName String getName() {
            return null;
        }
    }

    class StepTypeConverter implements CustomReferenceConverter<String> {
        @Override
        public PsiReference @NotNull [] createReferences(GenericDomValue<String> value, PsiElement element, ConvertContext context) {
            return new PsiPolyVariantReferenceBase[]{new PsiPolyVariantReferenceBase<>(element, element.getTextRange(), true) {
                @Override public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
                    String type = value.getStringValue();
                    return PdiFacet.getInstance(element).stream()
                            .flatMap(pdiFacet -> pdiFacet.findStepMetaClasses(type, element.getResolveScope()))
                            .map(PsiElementResolveResult::new)
                            .toArray(ResolveResult[]::new);
                }
            }};
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    class StepInlayHintsProvider implements InlayHintsProvider<NoSettings> {


        private static final SettingsKey<NoSettings> SETTINGS_KEY = new SettingsKey<>("Step Hints");

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
                        if (to.equals(step)) {
                            int offset = xmlTag.getTextOffset();
                            Document document = editor1.getDocument();
                            int line = document.getLineNumber(offset);
                            int startOffset = document.getLineStartOffset(line);
                            int column = offset - startOffset;
                            PresentationFactory factory = this.getFactory();
                            InlayPresentation smallText = factory.smallText(from.getNameUntrimmed() + " \u2192");
                            InlayPresentation mouseHandling = factory.mouseHandling(smallText, (mouseEvent, point) -> ((NavigatablePsiElement)from.getXmlTag()).navigate(true), null);
                            InlayPresentation inlayPresentation = new SequencePresentation(List.of(factory.textSpacePlaceholder(column, true), mouseHandling));
                            sink.addBlockElement(offset, false, true, BlockInlayPriority.CODE_VISION, inlayPresentation);
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
                            InlayPresentation smallText = factory.smallText("\u2192 " + to.getNameUntrimmed());
                            InlayPresentation mouseHandling = factory.mouseHandling(smallText, (mouseEvent, point) -> ((NavigatablePsiElement)to.getXmlTag()).navigate(true), null);
                            InlayPresentation inlayPresentation = new SequencePresentation(List.of(factory.textSpacePlaceholder(column, true), mouseHandling));
                            sink.addBlockElement(nextLineStartOffset, true, true, BlockInlayPriority.CODE_VISION, inlayPresentation);
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
            return language.is(TransformationLanguage.INSTANCE);
        }

        @Override
        public boolean isVisibleInSettings() {
            return false;
        }
    }
}
/*
    <GUI>
      <xloc>64</xloc>
      <yloc>53</yloc>
      <draw>Y</draw>
    </GUI>
 */