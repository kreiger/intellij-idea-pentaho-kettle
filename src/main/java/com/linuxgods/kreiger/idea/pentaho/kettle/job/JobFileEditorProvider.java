package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.openapi.fileEditor.TextEditorWithPreview.Layout.SHOW_PREVIEW;

public class JobFileEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFileEditorProvider.class);
    private static final String EDITOR_TYPE_ID = "kjb";

    @Override public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof JobFileType;
    }

    @Override public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditor editor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        return new KettleTextEditorWithPreview(editor, new JobFileEditor(project, file), "KjbEditor");
    }

    @Override public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
