package com.linuxgods.kreiger.idea.pentaho.kettle.facet;

import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.patterns.ElementPattern;
import com.intellij.util.indexing.FileContent;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.JobFileType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.TransformationFileType;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.StandardPatterns.string;

public class PdiJobFrameworkDetector extends FacetBasedFrameworkDetector<PdiFacet, PdiFacetConfiguration> {
    public PdiJobFrameworkDetector() {
        super("PentahoKettleJob");
    }

    @Override public @NotNull FacetType<PdiFacet, PdiFacetConfiguration> getFacetType() {
        return PdiFacetType.INSTANCE;
    }

    @Override public @NotNull FileType getFileType() {
        return JobFileType.INSTANCE;
    }

    @Override public @NotNull ElementPattern<FileContent> createSuitableFilePattern() {
        return FileContentPattern.fileContent()
                .withName(string().endsWith(".kjb"))
                .xmlWithRootTag("job");
    }

    @Override public void setupFacet(@NotNull PdiFacet facet, ModifiableRootModel model) {
        System.out.println("setupFacet");
    }
}
