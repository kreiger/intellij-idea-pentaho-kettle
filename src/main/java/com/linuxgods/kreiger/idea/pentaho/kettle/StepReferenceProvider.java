package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step.StepIndex;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StepReferenceProvider extends PsiReferenceProvider {

    private final Function<PsiElement, GlobalSearchScope> scope;

    public StepReferenceProvider(Function<PsiElement, GlobalSearchScope> scope) {
        this.scope = scope;
    }

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return JavaReferenceContributor.getLiteralString(element)
                .map(stepName -> StepIndex.findStepsByName(element.getProject(), stepName, scope.apply(element))
                        .map(Step::getFakePsiElement)
                        .map(psiElement -> new PsiReferenceBase.Immediate<>(element, psiElement))
                        .toArray(PsiReference[]::new))
                .orElse(PsiReference.EMPTY_ARRAY);
    }

}
