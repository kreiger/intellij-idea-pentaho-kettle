package com.linuxgods.kreiger.idea.pentaho.kettle.transformation;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.IconManager;
import com.linuxgods.kreiger.idea.pentaho.kettle.PentahoKettleFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TransformationFileType extends PentahoKettleFileType {
    public final static TransformationFileType INSTANCE = new TransformationFileType();

    protected TransformationFileType() {
        super(TransformationLanguage.INSTANCE);
    }

    @Override public @NonNls @NotNull String getName() {
        return "KTR";
    }

    @Override public @NlsContexts.Label @NotNull String getDescription() {
        return "Pentaho Kettle Transformation";
    }

    @Override public @NlsSafe @NotNull String getDefaultExtension() {
        return "ktr";
    }

    @Override public @Nullable Icon getIcon() {
        return IconManager.getInstance().getIcon("/ui/images/TRN.svg", getClass());
    }

    @Override public boolean isSecondary() {
        return super.isSecondary();
    }
}
