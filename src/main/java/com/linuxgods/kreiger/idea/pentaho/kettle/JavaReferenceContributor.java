package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.codeInspection.i18n.JavaI18nUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.util.ProcessingContext;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step.StepIndex;
import org.jetbrains.annotations.NotNull;

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
        registrar.registerReferenceProvider(JAVA_LITERAL, new StepReferenceProvider());
    }

    public static class StepReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            String stepName = null;
            if (!(element instanceof PsiLiteralExpression)) {
                return PsiReference.EMPTY_ARRAY;
            }
            PsiLiteralExpression literal = (PsiLiteralExpression) element;
            Object value = literal.getValue();
            if (value instanceof String) {
                stepName = (String)value;
            }
            if (stepName == null) return PsiReference.EMPTY_ARRAY;

            return StepIndex.findStepsByName(element.getProject(), stepName, element.getResolveScope())
                    .map(step -> new PsiReferenceBase.Immediate<>(element, step.getFakePsiElement()))
                    .toArray(PsiReference[]::new);
        }

    }
}
