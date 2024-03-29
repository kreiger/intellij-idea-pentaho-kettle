package com.linuxgods.kreiger.idea.pentaho.kettle.spoon;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.JavaSdkVersion;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.pentaho.kettle.PentahoKettleFileType;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacetConfiguration;
import com.linuxgods.kreiger.idea.pentaho.kettle.facet.PdiFacetType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OpenInSpoonAction extends AnAction {

    @Override public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = null != file && file.getFileType() instanceof PentahoKettleFileType;

        Presentation presentation = e.getPresentation();
        presentation.setDisabledIcon(presentation.getIcon());
        presentation.setVisible(visible);

        Optional<VirtualFile> spoon = getSpoon(e.getProject(), file);
        if (spoon.isEmpty()) {
            presentation.setEnabled(false);
            presentation.setDescription("There is no spoon");
        }
        Optional<String> jdk8Home = getJdkHomeByVersion(JavaSdkVersion.JDK_1_8);
        if (jdk8Home.isEmpty()) {
            presentation.setEnabled(false);
            presentation.setDescription("Can't open in Spoon, JDK 8 is not present.");
        }
    }

    private Optional<VirtualFile> getSpoon(Project project, VirtualFile file) {
        if (null == project || null == file) {
            return Optional.empty();
        }
        return Optional.ofNullable(ProjectFileIndex.getInstance(project).getModuleForFile(file))
                .map(module -> FacetManager.getInstance(module).getFacetByType(PdiFacetType.ID))
                .map(Facet::getConfiguration)
                .map(PdiFacetConfiguration::getSdk)
                .map(Sdk::getHomeDirectory)
                .map(sdkDirectory -> sdkDirectory.findChild(SystemInfo.isWindows ? "Spoon.bat" : "spoon.sh"));
    }

    @Override public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) return;
        getSpoon(e.getProject(), file).ifPresent(spoon -> {
            try {
                var commandLine = new GeneralCommandLine(spoon.getCanonicalPath(), "-file", file.getCanonicalPath());
                Optional<String> jdk8Home = getJdkHomeByVersion(JavaSdkVersion.JDK_1_8);
                jdk8Home.ifPresent(homePath -> {
                    commandLine.withEnvironment("JAVA_HOME", homePath);
                });
                commandLine.createProcess();
            } catch (ExecutionException executionException) {
                throw new RuntimeException(executionException);
            }
        });
    }

    @NotNull private Optional<String> getJdkHomeByVersion(JavaSdkVersion jdkVersion) {
        JavaSdk jdkType = JavaSdk.getInstance();
        return ProjectJdkTable.getInstance().getSdksOfType(jdkType).stream()
                .filter(jdk -> jdkVersion.equals(jdkType.getVersion(jdk)))
                .filter(jdk -> jdk.getHomePath() != null)
                .filter(jdk -> jdkType.isValidSdkHome(jdk.getHomePath()))
                .findFirst()
                .map(Sdk::getHomePath);
    }
}
