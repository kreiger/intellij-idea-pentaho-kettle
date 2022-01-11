package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.codeInsight.daemon.*;
import com.intellij.ide.IconProvider;
import com.intellij.ide.presentation.PresentationProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.paths.PathReference;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.xml.*;
import com.intellij.util.xml.converters.PathReferenceConverter;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.OptionalInt;

import static com.intellij.patterns.XmlPatterns.xmlTag;
import static com.intellij.patterns.XmlPatterns.xmlText;
import static java.util.stream.Collectors.toList;

public interface Step extends DomElement {

    @PropertyAccessor("name")
    @NameValue
    @Required
    default String getName() {
        XmlTag xmlTag = getXmlTag();
        if (xmlTag == null) return null;
        String nameText = xmlTag.getSubTagText("name");
        if (nameText == null) return null;
        return StringEscapeUtils.unescapeXml(nameText);
    }

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
            PdiFacet pdiFacet = PdiFacet.getInstance(step.getModule());

            String type = step.getType().getStringValue();
            Icon icon = pdiFacet.getIcon(type).orElse(null);
            return icon;
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
                    PdiFacet pdiFacet = PdiFacet.getInstance(element.getProject(), element.getContainingFile().getVirtualFile());
                    return pdiFacet.getIcon(type).orElse(null);
                }
            }
            return null;
        }
    }

    class StepLineMarkerProvider extends LineMarkerProviderDescriptor {

        public static final XmlElementPattern.@NotNull XmlTextPattern STEP_TYPE_PATTERN = xmlText()
                .withParent(xmlTag().withLocalName("type")
                        .withParent(xmlTag().withLocalName("step")));

        @Override public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
            if (!STEP_TYPE_PATTERN.accepts(element)) return null;
            XmlText xmlText = (XmlText) element;
            String type = xmlText.getValue();
            System.out.println("Type: " + type);
            PdiFacet pdiFacet = PdiFacet.getInstance(element);
            return pdiFacet.getIcon(type)

                    .map(icon -> {
                        List<PsiClass> stepMetaClasses = pdiFacet.findStepMetaClasses(type, element.getResolveScope()).collect(toList());
                        LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(), icon, null, new DefaultGutterIconNavigationHandler<>(stepMetaClasses, type), GutterIconRenderer.Alignment.RIGHT, () -> type);
                        return NavigateAction.setNavigateAction(lineMarkerInfo, "Go To "+type, "GotoClass", icon);
                    })
                    .orElse(null);

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
                    System.out.println("multiResolve: "+value+" "+type);
                    return PdiFacet.getInstance(element).findStepMetaClasses(type, element.getResolveScope())
                            .map(PsiElementResolveResult::new)
                            .toArray(ResolveResult[]::new);
                }
            }};
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