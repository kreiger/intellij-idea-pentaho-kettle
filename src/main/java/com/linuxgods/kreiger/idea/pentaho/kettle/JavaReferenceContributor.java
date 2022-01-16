package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.codeInspection.i18n.JavaI18nUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.*;

public class JavaReferenceContributor extends PsiReferenceContributor {

    private static final PsiElementPattern.@NotNull Capture<PsiLiteralExpression> JAVA_LITERAL = PlatformPatterns.psiElement(PsiLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
        @Override
        public boolean isAcceptable(Object element, PsiElement context) {
            return !JavaI18nUtil.mustBePropertyKey((PsiLiteralExpression) context, null);
        }

        @Override
        public boolean isClassAcceptable(Class hintClass) {
            return true;
        }
    }));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(JAVA_LITERAL, new StepReferenceProvider(PsiElement::getResolveScope));
        registrar.registerReferenceProvider(JAVA_LITERAL, new TransformationReferenceProvider());
    }

    static Optional<String> getLiteralString(PsiElement element) {
        if (element instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteral) element).getValue();
            if (value instanceof String) {
                return Optional.of((String) value);
            }
            return Optional.empty();
        } else if (element instanceof XmlTag) {
            XmlTag xmlTag = (XmlTag) element;
            String text = Arrays.stream(xmlTag.getValue().getTextElements()).map(XmlText::getValue).collect(joining());
            return Optional.of(text);
        }
        return Optional.empty();
    }

}
