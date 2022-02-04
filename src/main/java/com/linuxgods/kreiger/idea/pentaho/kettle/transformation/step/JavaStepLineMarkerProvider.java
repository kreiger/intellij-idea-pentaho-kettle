package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.*;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class JavaStepLineMarkerProvider implements LineMarkerProvider {
    @Override public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) return null;
        PdiFacet pdiFacet = PdiFacet.getInstance(element).orElse(null);
        if (null == pdiFacet) return null;
        PsiElement parent = element.getParent();
        if (parent == null) return null;
        /*
        if (parent instanceof PsiMethod) {
            PsiType type = ((PsiMethod) parent).getReturnType();
            StepType stepType = pdiFacet.getStepType(type).orElse(null);
            return getPsiElementLineMarkerInfo(element, stepType);
        }
         */
        PsiElement grandParent = parent.getParent();
        if (grandParent instanceof PsiTypeElement && grandParent.getParent() instanceof PsiClassObjectAccessExpression) {
            PsiTypeElement typeElement = (PsiTypeElement) grandParent;
            PsiType type = typeElement.getType();
            StepType stepType = pdiFacet.getStepType(type).orElse(null);
            return getPsiElementLineMarkerInfo(element, stepType);
        }
        if (grandParent instanceof PsiCallExpression) {
            PsiCallExpression callExpression = (PsiCallExpression) grandParent;
            StepType stepType = pdiFacet.getStepType(callExpression.getType()).orElse(null);
            if (stepType == null) return null;
            if (hasStepTypeArgument(pdiFacet, stepType, callExpression)) {
                return null;
            }

            return getPsiElementLineMarkerInfo(element, stepType);
        }
        return null;
    }

    private boolean hasStepTypeArgument(PdiFacet pdiFacet, StepType stepType, PsiCallExpression callExpression) {
        PsiExpressionList argumentList = callExpression.getArgumentList();
        if (argumentList == null) return false;
        for (PsiExpression expression : argumentList.getExpressions()) {
            StepType argumentStepType = pdiFacet.getStepType(expression.getType()).orElse(null);
            if (stepType.equals(argumentStepType)) {
                return true;
            }
            if (expression instanceof PsiMethodCallExpression) {
                if (hasStepTypeArgument(pdiFacet, stepType, (PsiMethodCallExpression) expression)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getPsiElementLineMarkerInfo(@NotNull PsiElement element, StepType stepType) {
        if (stepType == null) return null;

        List<NavigatablePsiElement> navigatablePsiElements = PdiFacet.findClasses(stepType.getClassName(), element.getProject(), element.getResolveScope())
                .flatMap(StepType::getTypePsiElements)
                .collect(toList());
        if (navigatablePsiElements.isEmpty()) return null;

        return stepType.getPsiElementLineMarkerInfo(element, navigatablePsiElements);
    }

}
