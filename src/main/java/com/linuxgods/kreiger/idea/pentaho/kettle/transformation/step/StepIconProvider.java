package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StepIconProvider extends IconProvider {
    @Override public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof XmlTag) {
            XmlTag xmlTag = (XmlTag) element;
            if ("type".equals(xmlTag.getName())) {
                String type = xmlTag.getValue().getTrimmedText();
                System.out.println("Icon: " + type);
                return PdiFacet.getInstance(element.getProject(), element.getContainingFile().getVirtualFile())
                        .flatMap(pdiFacet -> pdiFacet.getStepType(type).map(StepType::getIcon))
                        .orElse(null);
            }
        }
        return null;
    }
}
