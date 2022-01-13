package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step;

import com.intellij.codeInspection.i18n.JavaI18nUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import org.jetbrains.annotations.NotNull;

public class StepReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(PsiLiteralExpression.class).and(new FilterPattern(new ElementFilter() {
            @Override
            public boolean isAcceptable(Object element, PsiElement context) {
                return !JavaI18nUtil.mustBePropertyKey((PsiLiteralExpression) context, null);
            }

            @Override
            public boolean isClassAcceptable(Class hintClass) {
                return true;
            }
        })), new StepReferenceProvider());
    }
}
