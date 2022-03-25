package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.step;

import com.intellij.ide.presentation.PresentationProvider;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacet;
import com.linuxgods.kreiger.idea.pentaho.kettle.sdk.StepType;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Optional;

public class StepPresentationProvider extends PresentationProvider<Step> {
    @Override public @Nullable String getName(Step step) {
        return super.getName(step);
    }

    @Override public @Nullable Icon getIcon(Step step) {

        String type = step.getType().getStringValue();
        return Optional.ofNullable(step.getModule())
                .flatMap(PdiFacet::getInstance)
                .flatMap(pdiFacet -> pdiFacet.getStepType(type).map(StepType::getIcon))
                .orElse(null);
    }

    @Override public @Nullable @Nls(capitalization = Nls.Capitalization.Title) String getTypeName(Step step) {
        return "Step";
    }

}
