package com.linuxgods.kreiger.idea.pentaho.kettle;

import com.intellij.ide.highlighter.DomSupportEnabled;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.UIBasedFileType;
import org.jetbrains.annotations.NotNull;

public abstract class PentahoKettleFileType extends XmlLikeFileType implements UIBasedFileType, DomSupportEnabled {
    public PentahoKettleFileType(@NotNull Language language) {
        super(language);
    }
}
