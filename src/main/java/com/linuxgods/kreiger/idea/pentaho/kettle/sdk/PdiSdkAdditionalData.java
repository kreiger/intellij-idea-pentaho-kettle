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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class PdiSdkAdditionalData implements SdkAdditionalData {
    private final ClassLoader classLoader;
    private final Map<String, StepType> stepTypes;

    public PdiSdkAdditionalData(Map<String, StepType> stepTypes, @NotNull URLClassLoader classLoader) {
        this.classLoader = classLoader;
        this.stepTypes = stepTypes;
    }

    @NotNull public static URLClassLoader createClassLoader(List<URL> urls) {
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
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
        return getStepType(id)
                .map(StepType::getImagePath)
                .map(classLoader::getResource);
    }

    @NotNull public Optional<StepType> getStepType(String id) {
        return Optional.ofNullable(stepTypes.get(id));
    }


    public void save(Element rootElement) {
        Element steps = new Element("steps");
        rootElement.addContent(steps);
        this.stepTypes.forEach((id, stepType) -> {
            Element element = stepType.createElement();
            steps.addContent(element);
        });
    }

    public static SdkAdditionalData load(@NotNull Sdk sdk, Element rootElement) {
        List<URL> urls = Stream.of(sdk.getRootProvider().getUrls(OrderRootType.CLASSES))
                .map(PdiSdkAdditionalData::stringUrl)
                .collect(toList());
        Element steps = rootElement.getChild("steps");
        URLClassLoader classLoader = createClassLoader(urls);
        Map<String, StepType> stepsMap = steps.getChildren("step").stream()
                .map(element -> new StepType(element, path -> StepType.loadIcon(classLoader, path)))
                .collect(toMap(StepType::getId, Function.identity()));
        return new PdiSdkAdditionalData(stepsMap, classLoader);
    }

    public Stream<StepType> getStepTypes() {
        return stepTypes.values().stream().sorted(Comparator.comparing(StepType::getId));
    }
}
