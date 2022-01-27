package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.ide.highlighter.DomSupportEnabled;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.fileTypes.UIBasedFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JobFileType extends XmlLikeFileType implements UIBasedFileType, DomSupportEnabled {
    public final static JobFileType INSTANCE = new JobFileType();

    protected JobFileType() {
        super(JobLanguage.INSTANCE);
    }

    @Override public @NonNls @NotNull String getName() {
        return "KJB";
    }

    @Override public @NlsContexts.Label @NotNull String getDescription() {
        return "Pentaho Kettle Job";
    }

    @Override public @NlsSafe @NotNull String getDefaultExtension() {
        return "kjb";
    }

    @Override public @Nullable Icon getIcon() {
        return IconManager.getInstance().getIcon("/ui/images/JOB.svg", getClass());
    }

    @Override public boolean isSecondary() {
        return super.isSecondary();
    }
}
