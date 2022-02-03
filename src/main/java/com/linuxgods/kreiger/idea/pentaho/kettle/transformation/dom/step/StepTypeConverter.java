package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ResolvingConverter;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class StepTypeConverter extends ResolvingConverter<StepType> implements CustomReferenceConverter<StepType> {
    @Override public @NotNull Collection<? extends StepType> getVariants(ConvertContext context) {
        return PdiFacet.getInstance(context.getModule())
                .stream()
                .flatMap(PdiFacet::getStepTypes)
                .collect(toList());
    }

    @Override public @Nullable LookupElement createLookupElement(StepType s) {
        return new LookupElement() {
            @Override public @NotNull String getLookupString() {
                return s.getId();
            }

            @Override public void renderElement(LookupElementPresentation presentation) {
                presentation.setItemText(s.getId());
                presentation.setTypeText(s.getClassName());
                presentation.setIcon(s.getIcon());
            }
        };
    }

    @Override public @Nullable StepType fromString(@Nullable @NonNls String stepTypeId, ConvertContext context) {
        return PdiFacet.getInstance(context.getModule())
                .flatMap(pdiFacet -> pdiFacet.getStepType(stepTypeId))
                .orElse(null);
    }

    @Override public @Nullable String toString(@Nullable StepType stepType, ConvertContext context) {
        return null == stepType ? null : stepType.getId();
    }

    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<StepType> value, PsiElement element, ConvertContext context) {
        return PdiFacet.getInstance(element)
                .flatMap(pdiFacet -> pdiFacet.getStepType(value.getStringValue())).stream()
                .flatMap(stepType -> stepType.getTypePsiElements(element.getProject(), element.getResolveScope()))
                .map(psiElement -> new PsiReferenceBase.Immediate<>(element, psiElement))
                .toArray(PsiReference[]::new);
    }
}
