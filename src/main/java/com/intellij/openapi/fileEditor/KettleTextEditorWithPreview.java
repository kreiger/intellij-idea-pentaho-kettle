package com.intellij.openapi.fileEditor;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class KettleTextEditorWithPreview extends TextEditorWithPreview {
    public KettleTextEditorWithPreview(@NotNull TextEditor editor, @NotNull FileEditor preview, @Nls @NotNull String editorName) {
        super(editor, preview, editorName, Layout.SHOW_PREVIEW);
    }
    @Override
    public void setLayout(@NotNull Layout layout) {
        super.setLayout(layout);
    }
}
