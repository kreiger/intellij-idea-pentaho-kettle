package com.linuxgods.kreiger.idea.pentaho.kettle.sdk;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.SystemProperties;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static java.util.stream.StreamSupport.stream;

public class PdiSdkHomeGuesser {

    public Path getPdiHomeSuggestion() {
        Path path = Path.of(SystemProperties.getUserHome());
        Stream<Path> roots = getRoots();
        for (Path root : (Iterable<Path>) roots::iterator) {
            path = root;
            for (String name : List.of("Pentaho", "pentaho", "client-tools", "design-tools", "data-integration")) {
                Path newPath = path.resolve(name);
                if (isDirectory(newPath)) {
                    path = newPath;
                }
            }
            if (path != root) {
                break;
            }
        }
        return path;
    }



    private Stream<Path> getRoots() {
        String userHome = SystemProperties.getUserHome();
        if (SystemInfo.isWindows) {
            Spliterator<Path> rootPaths = FileSystems.getDefault().getRootDirectories().spliterator();
            return Stream.concat(Stream.concat(
                    dir(System.getenv("ProgramFiles")).stream(),
                    stream(rootPaths, false)),
                    dir(userHome).stream());
        }
        if (SystemInfo.isMac) {
            return Stream.of(dir("/Applications"),
                            dir(userHome))
                    .flatMap(Optional::stream);
        }
        return Stream.of(
                        dir(userHome, "opt"),
                        dir("/opt"),
                        dir("/usr", "local"),
                        dir(userHome))
                .flatMap(Optional::stream);
    }

    private Optional<Path> dir(String first, String... rest) {
        if (null == first) {
            return Optional.empty();
        }
        Path path = Path.of(first);
        if (!isDirectory(path)) {
            return Optional.empty();
        }
        for (String next : rest) {
            Path nextPath = path.resolve(next);
            if (!isDirectory(nextPath)) {
                return Optional.empty();
            }
            path = nextPath;
        }
        return Optional.of(path);
    }

}
