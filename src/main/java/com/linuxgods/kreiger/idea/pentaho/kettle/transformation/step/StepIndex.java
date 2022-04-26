package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationFileType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

public class StepIndex extends ScalarIndexExtension<String> {

    private static final @NotNull ID<String, Void> NAME = ID.create("PDITransformationStepIndex");

    public static @NotNull Stream<Step> findStepsByName(Project project, String stepName, GlobalSearchScope filter) {
        return FileBasedIndex.getInstance().getContainingFiles(NAME, stepName, filter)
                .stream()
                .flatMap(file -> Transformation.getTransformation(project, file).stream())
                .flatMap(transformation -> transformation.getSteps().stream())
                .filter(step -> stepName.equals(step.getNameUntrimmed()));
    }

    @Override
    public @NotNull ID<String, Void> getName() {
        return NAME;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(TransformationFileType.INSTANCE) {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return super.acceptInput(file);
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            VirtualFile file = inputData.getFile();
            return Transformation.getTransformation(inputData.getProject(), file).stream()
                    .flatMap(transformation -> transformation.getSteps().stream())
                    .map(Step::getNameUntrimmed)
                    .collect(HashMap::new, (map, name) -> map.put(name, null), HashMap::putAll);
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }


}
