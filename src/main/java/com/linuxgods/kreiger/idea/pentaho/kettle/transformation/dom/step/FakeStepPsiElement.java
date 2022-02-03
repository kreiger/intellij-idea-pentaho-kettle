package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.step;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class FakeStepPsiElement extends FakePsiElement {
    private final Step step;

    public FakeStepPsiElement(Step step) {
        this.step = step;
    }

    @Override
    public PsiElement getParent() {
        return step.getXmlTag();
    }

    @Override
    public @Nullable Icon getIcon(boolean open) {
        return step.getIcon().orElse(null);
    }

    @Override
    public String getPresentableText() {
        return step.getNameUntrimmed();
    }

    @Override
    public @NlsSafe @Nullable String getLocationString() {
        VirtualFile file = getNavigationElement().getContainingFile().getVirtualFile();
        return VfsPresentationUtil.getUniquePresentableNameForUI(getProject(), file);
    }

    @Override
    public @NotNull PsiElement getNavigationElement() {
        return step.getName().getXmlTag();
    }
}
