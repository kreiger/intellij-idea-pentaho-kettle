package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.ImageLoader;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class PdiSdkAdditionalData implements SdkAdditionalData {
    private final ClassLoader classLoader;
    private final Map<String, Step> steps;

    public PdiSdkAdditionalData(Map<String, Step> steps, @NotNull URLClassLoader classLoader) {
        this.classLoader = classLoader;
        this.steps = steps;
    }

    @NotNull public static URLClassLoader createClassLoader(List<URL> urls) {
        return new URLClassLoader(urls.toArray(new URL[0]), PdiSdkAdditionalData.class.getClassLoader());
    }



    @NotNull static URL pathUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static URL stringUrl(String s) {
        return VfsUtilCore.convertToURL(s);
    }

    public Optional<Image> getImage(String id) {
        return getImageResource(id)
                .map(ImageLoader::loadFromUrl);
    }

    @NotNull private Optional<URL> getImageResource(String id) {
        return getStep(id)
                .map(Step::getImage)
                .map(classLoader::getResource);
    }

    @NotNull public Optional<Step> getStep(String id) {
        return Optional.ofNullable(steps.get(id));
    }

    public void save(Element rootElement) {
        Element steps = new Element("steps");
        rootElement.addContent(steps);
        this.steps.forEach((id, step) -> {
            Element element = step.createElement();
            steps.addContent(element);
        });
    }

    public static SdkAdditionalData load(@NotNull Sdk sdk, Element rootElement) {
        List<URL> urls = Stream.of(sdk.getRootProvider().getUrls(OrderRootType.CLASSES))
                .map(PdiSdkAdditionalData::stringUrl)
                .collect(toList());
        Element steps = rootElement.getChild("steps");
        Map<String, Step> stepsMap = steps.getChildren("step").stream()
                .map(Step::new)
                .collect(toMap(Step::getId, Function.identity()));
        return new PdiSdkAdditionalData(stepsMap, createClassLoader(urls));
    }

}
