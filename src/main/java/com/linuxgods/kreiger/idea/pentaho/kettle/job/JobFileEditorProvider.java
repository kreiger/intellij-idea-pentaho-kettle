package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobFileEditorProvider implements FileEditorProvider, DumbAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobFileEditorProvider.class);
    private static final String EDITOR_TYPE_ID = "kjb";

    @Override public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        boolean jobType = file.getFileType() instanceof JobFileType;
        boolean sdkPresent = PdiFacet.getInstance(project, file).flatMap(PdiFacet::getSdkAdditionalData).isPresent();
        boolean accepted = jobType && sdkPresent;
        //LOGGER.warn("accepted "+jobType+" "+sdkPresent+" "+file);
        return accepted;
    }

    @Override public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        PdiFacet pdiFacet = PdiFacet.getInstance(project, file).orElseThrow();
        return new JobFileEditor(project, file, pdiFacet);
    }

    @Override public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}
