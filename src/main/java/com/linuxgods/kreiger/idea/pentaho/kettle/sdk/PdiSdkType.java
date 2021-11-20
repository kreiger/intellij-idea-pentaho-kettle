package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.KettleIcons;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static com.linuxgods.kreiger.idea.pentaho.kettle.sdk.AnnotationsScanner.scanAnnotations;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

public class PdiSdkType extends SdkType {
    public final static PdiSdkType INSTANCE = new PdiSdkType();

    public PdiSdkType() {
        super("PentahoDataIntegration");
    }

    @Override public Icon getIcon() {
        return KettleIcons.KETTLE_ICON;
    }

    @Override public @Nullable String suggestHomePath() {
        return new PdiSdkHomeGuesser().getPdiHomeSuggestion().toString();
    }

    @Override public @Nullable String getDownloadSdkUrl() {
        return "https://sourceforge.net/projects/pentaho/files/";
    }

    @Override public boolean setupSdkPaths(@NotNull Sdk sdk, @NotNull SdkModel sdkModel) {
        SdkModificator modificator = sdk.getSdkModificator();
        Path kettleHome = Path.of(modificator.getHomePath());
        modificator.setVersionString(getVersionString(sdk));
        List<Path> libJars = getLibJars(kettleHome).collect(toList());
        libJars.forEach(libJar -> modificator.addRoot(VfsUtil.getUrlForLibraryRoot(libJar.toFile()), OrderRootType.CLASSES));
        Stream<Path> pluginsJars = getPluginsJars(kettleHome);
        List<Path> paths = Stream.concat(libJars.stream(), pluginsJars).collect(toList());
        List<URL> urls = paths.stream()
                .map(PdiSdkAdditionalData::pathUrl)
                .collect(toList());
        Map<String, Step> steps = new HashMap<>();
        ProgressManager.getInstance().run(new Task.Modal(null, "Scanning Pentaho PDI for steps", false) {
            @Override public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                for (int i = 0, pathsSize = paths.size(); i < pathsSize; i++) {
                    indicator.setFraction(i / (float) urls.size());
                    Path path = paths.get(i);
                    URL url = PdiSdkAdditionalData.pathUrl(path);
                    Map<String, Step> urlSteps = new HashMap<>();
                    scanAnnotations(url, (annotation, s) ->
                            createStep(annotation)
                                    .ifPresent(step -> urlSteps.put(step.getId(), step)));
                    if (!urlSteps.isEmpty()) {
                        String pluginClassUrl = VfsUtil.getUrlForLibraryRoot(path.toFile());
                        modificator.addRoot(pluginClassUrl, OrderRootType.CLASSES);
                        steps.putAll(urlSteps);
                    }
                }
                indicator.setFraction(1);
            }
        });
        URLClassLoader classLoader = PdiSdkAdditionalData.createClassLoader(urls);
        InputStream kettleStepsXml = classLoader.getResourceAsStream("kettle-steps.xml");
        KettleIcons.loadStepsXml(kettleStepsXml)
                .forEach(step -> steps.put(step.getId(), step));
        modificator.setSdkAdditionalData(new PdiSdkAdditionalData(steps, classLoader));
        modificator.commitChanges();
        return true;
    }

    private Stream<Path> getPluginsJars(Path kettleHome) {
        Path pluginsDir = kettleHome.resolve("plugins");
        Stream<Path> pluginsJars = findJars(pluginsDir);
        return pluginsJars;
    }

    @NotNull private Stream<Path> getLibJars(Path kettleHome) {
        try {
            Path libDir = kettleHome.resolve("lib");
            PathMatcher pathMatcher = libDir.getFileSystem().getPathMatcher("glob:**/kettle-{engine,ui-swt}-*.jar");
            return Files.list(libDir).filter(pathMatcher::matches);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Step> createStep(Annotation annotation) {
        Set<String> memberNames = annotation.getMemberNames();
        if (memberNames != null && memberNames.contains("id") && memberNames.contains("image")) {
            var id = ((StringMemberValue) annotation.getMemberValue("id")).getValue();
            var image = ((StringMemberValue) annotation.getMemberValue("image")).getValue();
            return Optional.of(new Step(id, image));
        }
        return Optional.empty();
    }

    private Stream<Path> findJars(Path plugins) {
        try {
            return Files.walk(plugins)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    ;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public String getInvalidHomeMessage(@NotNull String path) {
        return super.getInvalidHomeMessage(path) + " Download it from " + getDownloadSdkUrl();
    }

    @Override public boolean isValidSdkHome(@NotNull String path) {
        Path kettleHome = Paths.get(path);
        return isValidSdkHome(kettleHome);
    }

    private boolean isValidSdkHome(Path kettleHome) {
        Optional<String> uiJarVersion = getUiJarVersion(kettleHome);
        return uiJarVersion.isPresent() && Files.isDirectory(kettleHome.resolve("plugins"));
    }

    @Override public @NotNull String adjustSelectedSdkHome(@NotNull String homePath) {
        Path diPath = Path.of(homePath, "data-integration");
        if (Files.exists(diPath) && isValidSdkHome(diPath)) {
            return diPath.toString();
        }
        return homePath;
    }

    @Override public @Nullable String getVersionString(String sdkHome) {
        return getUiJarVersion(Path.of(sdkHome)).orElse(null);
    }

    @NotNull private Optional<String> getUiJarVersion(Path kettleHome) {
        try {
            Path lib = kettleHome.resolve("lib");
            if (!Files.exists(lib)) {
                return Optional.empty();
            }
            PathMatcher pathMatcher = kettleHome.getFileSystem().getPathMatcher("glob:kettle-ui-swt-*.jar");
            return Files.list(lib)
                    .map(lib::relativize)
                    .filter(pathMatcher::matches)
                    .map(uiJar -> {
                        String fileName = uiJar.getFileName().toString();
                        return fileName.substring("kettle-ui-swt-".length(), fileName.length() - ".jar".length());
                    })
                    .findFirst();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public @NotNull String suggestSdkName(@Nullable String currentSdkName, @NotNull String sdkHome) {
        return "Pentaho PDI" + getUiJarVersion(Path.of(sdkHome)).map(v -> " " + v).orElse("");
    }

    @Override
    public @Nullable AdditionalDataConfigurable createAdditionalDataConfigurable(@NotNull SdkModel sdkModel, @NotNull SdkModificator sdkModificator) {
        return null;
    }

    @Override public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getPresentableName() {
        return "Pentaho PDI";
    }

    @Override public void saveAdditionalData(@NotNull SdkAdditionalData additionalData, @NotNull Element element) {
        ((PdiSdkAdditionalData) additionalData).save(element);
    }

    @Override
    public SdkAdditionalData loadAdditionalData(@NotNull Sdk sdk, @NotNull Element element) {
        return PdiSdkAdditionalData.load(sdk, element);
    }
}
