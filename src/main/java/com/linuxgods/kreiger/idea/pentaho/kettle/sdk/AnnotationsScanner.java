package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import javassist.bytecode.annotation.Annotation;
import org.jetbrains.annotations.NotNull;
import org.scannotation.AnnotationDB;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.function.BiConsumer;

public class AnnotationsScanner {

    public static void scanAnnotations(URL url, BiConsumer<Annotation, String> consumer) {
        AnnotationDB annotationDB = createAnnotationDB(consumer);
        try {
            annotationDB.scanArchives(url);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @NotNull private static AnnotationDB createAnnotationDB(BiConsumer<Annotation, String> consumer) {
        AnnotationDB annotationDB = new AnnotationDB() {
            @Override
            protected void populate(javassist.bytecode.annotation.Annotation[] annotations, String className) {
                for (javassist.bytecode.annotation.Annotation annotation : annotations) {
                    consumer.accept(annotation, className);
                }
            }
        };
        annotationDB.setScanMethodAnnotations(false);
        annotationDB.setScanFieldAnnotations(false);
        annotationDB.setScanParameterAnnotations(false);
        return annotationDB;
    }
}
