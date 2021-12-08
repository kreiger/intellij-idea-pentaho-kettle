package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TransformationFileEditorProvider implements FileEditorProvider, DumbAware {

    private static final String EDITOR_TYPE_ID = "ktr";

    @Override public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof TransformationFileType;
    }

    @Override public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new TransformationFileEditor(project, file);
    }

    @Override public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}
