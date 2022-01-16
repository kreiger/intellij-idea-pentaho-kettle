package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

class TransformationReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {

        return JavaReferenceContributor.getLiteralString(element)
                .stream()
                .map(name -> name.endsWith(".ktr") ? name : name + ".ktr")
                .flatMap(name1 -> Arrays.stream(FilenameIndex.getFilesByName(element.getProject(), name1, element.getResolveScope())))
                .map(file -> new PsiReferenceBase.Immediate<>(element, file))
                .toArray(PsiReference[]::new);
    }
}
