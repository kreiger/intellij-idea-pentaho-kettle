package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NotNull;

import static com.linuxgods.kreiger.idea.pentaho.kettle.JavaReferenceContributor.getLiteralString;

public class StepTypeReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        return getLiteralString(element)
                .map(typeName -> PdiFacet.getInstance(element).stream()
                        .flatMap(pdiFacet -> pdiFacet.getTypePsiElements(typeName, element.getResolveScope()))
                        .map(psiElement -> new PsiReferenceBase.Immediate<>(element, psiElement))
                        .toArray(PsiReference[]::new))
                .orElse(PsiReference.EMPTY_ARRAY);
    }
}
