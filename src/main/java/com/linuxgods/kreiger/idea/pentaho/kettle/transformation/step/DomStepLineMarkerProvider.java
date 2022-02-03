package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.codeInsight.daemon.*;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlTokenType;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.XmlPatterns.xmlTag;
import static com.intellij.patterns.XmlPatterns.xmlText;

public class DomStepLineMarkerProvider extends LineMarkerProviderDescriptor {

    public static final PsiElementPattern.Capture<PsiElement> STEP_TYPE_PATTERN = psiElement().withElementType(XmlTokenType.XML_DATA_CHARACTERS).inside(xmlText()
            .withParent(xmlTag().withLocalName("type")
                    .withParent(xmlTag().withLocalName("step"))));

    @Override public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!STEP_TYPE_PATTERN.accepts(element)) return null;
        XmlText xmlText = (XmlText) element.getParent();
        String typeId = xmlText.getValue();
        return PdiFacet.getInstance(element)
                .flatMap(pdiFacet -> pdiFacet.getStepType(typeId)
                        .map(type -> type.getPsiElementLineMarkerInfo(element, pdiFacet)))
                .orElse(null);

    }

    @Override public @Nullable("null means disabled") @GutterName String getName() {
        return null;
    }
}
