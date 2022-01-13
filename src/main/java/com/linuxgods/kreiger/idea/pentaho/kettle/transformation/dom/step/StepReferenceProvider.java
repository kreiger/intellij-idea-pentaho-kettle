package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.impl.GenericDomValueReference;
import org.intellij.plugins.intelliLang.util.StringLiteralReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StepReferenceProvider extends PsiReferenceProvider {
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
                .map(step -> new PsiReferenceBase.Immediate<>(element, step.getXmlTag().getContainingFile()))
                .toArray(PsiReference[]::new);
    }

}
