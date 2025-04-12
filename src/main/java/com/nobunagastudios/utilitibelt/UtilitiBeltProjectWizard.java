package com.nobunagastudios.utilitibelt;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.observable.properties.PropertyGraph;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.dsl.builder.Panel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class UtilitiBeltProjectWizard implements LanguageGeneratorNewProjectWizard {
    @Override
    public @NotNull Icon getIcon() {
        return AllIcons.General.Information;
    }

    @Override
    public @NotNull String getName() {
        return "utiLITI Belt";
    }

    @Override
    public @NotNull NewProjectWizardStep createStep(@NotNull NewProjectWizardStep newProjectWizardStep) {
        return new NewProjectWizardStep() {
            @Override
            public @NotNull UserDataHolder getData() {
                return newProjectWizardStep.getData();
            }

            @Override
            public @NotNull Keywords getKeywords() {
                return newProjectWizardStep.getKeywords();
            }

            @Override
            public @NotNull PropertyGraph getPropertyGraph() {
                return newProjectWizardStep.getPropertyGraph();
            }

            @Override
            public @NotNull WizardContext getContext() {
                return newProjectWizardStep.getContext();
            }

            @Override
            public void setupProject(@NotNull Project project) {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    try {
                        String projectName = getContext().getProjectName();

                        String basePath = getContext().getProjectFileDirectory();
                        VirtualFile baseDir = VfsUtil.createDirectoryIfMissing(basePath);
                        if (baseDir == null) throw new IOException("Failed to create base directory");

                        VirtualFile src = VfsUtil.createDirectoryIfMissing(baseDir, "src");
                        VirtualFile main = VfsUtil.createDirectoryIfMissing(src, "main");
                        VirtualFile java = VfsUtil.createDirectoryIfMissing(main, "java");
                        VirtualFile resources = VfsUtil.createDirectoryIfMissing(main, "resources");
                        VfsUtil.createDirectoryIfMissing(resources, "audio");
                        VfsUtil.createDirectoryIfMissing(resources, "sprites");
                        VfsUtil.createDirectoryIfMissing(resources, "maps");
                        VfsUtil.createDirectoryIfMissing(resources, "localization");
                        VfsUtil.createDirectoryIfMissing(resources, "misc");

                        VirtualFile mainFile = java.createChildData(this, "Main.java");
                        mainFile.setBinaryContent((
                                "import de.gurkenlabs.litiengine.*;\n" +
                                "import de.gurkenlabs.litiengine.resources.Resources;\n\n" +
                                "/**\n" +
                                "*\n" +
                                "*@see <a href=\"https://litiengine.com/docs/\">LITIENGINE Documentation</a>\n" +
                                "*\n" +
                                "*/\n\n" +
                                "public class Main {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        // set meta information about the game\n" +
                                "        Game.info().setName(\""+projectName+"\");\n" +
                                "        Game.info().setSubTitle(\"\");\n" +
                                "        Game.info().setVersion(\"v0.0.1\");\n" +
                                "        Game.info().setWebsite(\"link to game\");\n" +
                                "        Game.info().setDescription(\"A 2D Game made in the LITIENGINE\");\n\n" +

                                "        // init the game infrastructure\n" +
                                "        Game.init(args);\n\n" +

                                "        // set the icon for the game (this has to be done after initialization because the ScreenManager will not be present otherwise)\n" +
                                "        // Game.window().setIcon(Resources.images().get(\"path/to/icon/image\"));\n" +
                                "        Game.graphics().setBaseRenderScale(4f);\n\n" +

                                "        // load data from the utiLITI game file\n" +
                                "        // Resources.load(\"game.litidata\");\n\n" +

                                "        // load the first level (resources for the map were implicitly loaded from the game file)\n" +
                                "        // Game.world().loadEnvironment(\"path/to/level\");\n" +
                                "        Game.start();\n" +
                                "    }\n" +
                                "}"
                        ).getBytes());

                        VirtualFile buildGradle = baseDir.createChildData(this, "build.gradle");
                        buildGradle.setBinaryContent((
                                "plugins {\n" +
                                        "    id 'java'\n" +
                                        "    id 'application'\n" +
                                        "}\n\n" +
                                        "group 'com.example'\n" +
                                        "version '1.0'\n\n" +
                                        "repositories {\n" +
                                        "    mavenCentral()\n" +
                                        "    maven { url 'https://maven.pkg.jetbrains.space/litiengine/p/maven/releases' }\n" +
                                        "}\n\n" +
                                        "dependencies {\n" +
                                        "    implementation 'de.gurkenlabs:litiengine:0.8.0'\n" +
                                        "}\n\n" +
                                        "application {\n" +
                                        "    mainClass = 'Main'\n" +
                                        "}"
                        ).getBytes());

                        VirtualFile settingsGradle = baseDir.createChildData(this, "settings.gradle");
                        settingsGradle.setBinaryContent(
                                ("rootProject.name = '" + projectName + "'").getBytes()
                        );

                    } catch (IOException e) {
                        throw new RuntimeException("Failed to generate LITIENGINE project", e);
                    }
                });
            }

            @Override
            public void setupUI(@NotNull Panel builder) {
                NewProjectWizardStep.super.setupUI(builder);
            }
        };
    }
}
