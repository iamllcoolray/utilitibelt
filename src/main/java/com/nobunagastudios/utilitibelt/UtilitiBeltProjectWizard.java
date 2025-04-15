package com.nobunagastudios.utilitibelt;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.observable.properties.GraphProperty;
import com.intellij.openapi.observable.properties.PropertyGraph;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.dsl.builder.Panel;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class UtilitiBeltProjectWizard implements LanguageGeneratorNewProjectWizard {
    @Override
    public @NotNull Icon getIcon() {
        return IconLoader.getIcon("/icons/liti-logo-x16.png", getClass());
    }

    @Override
    public @NotNull String getName() {
        return "utiLITI Belt";
    }

    @Override
    public @NotNull NewProjectWizardStep createStep(@NotNull NewProjectWizardStep newProjectWizardStep) {
        return new NewProjectWizardStep() {
            final GraphProperty<String> packageNameProperty = getPropertyGraph().property("com.game");

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
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        String projectName = getContext().getProjectName();
                        Sdk selectedSdk = getContext().getProjectJdk();
                        String packageName = packageNameProperty.get();
                        String basePath = getContext().getProjectFileDirectory();

                        ApplicationManager.getApplication().invokeAndWait(() -> {
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                try {
                                    VirtualFile baseDir = VfsUtil.createDirectoryIfMissing(basePath);
                                    if (baseDir == null) throw new IOException("Failed to create base directory");

                                    VirtualFile java = createOrFindDirectory(baseDir, "src", "main", "java");
                                    VirtualFile resources = createOrFindDirectory(baseDir, "src", "main", "resources");

                                    String[] packagePath = packageName.split("\\.");
                                    VirtualFile packageDir = createOrFindDirectory(java, packagePath);

                                    createOrFindDirectory(resources, "audio");
                                    createOrFindDirectory(resources, "sprites");
                                    createOrFindDirectory(resources, "maps");
                                    createOrFindDirectory(resources, "localization");
                                    createOrFindDirectory(resources, "misc");

                                    VirtualFile mainFile = packageDir.createChildData(this, "Main.java");
                                    mainFile.setBinaryContent(buildMainClass(projectName, packageName).getBytes());

                                    VirtualFile buildGradle = baseDir.createChildData(this, "build.gradle");
                                    buildGradle.setBinaryContent(buildGradleContent(packageName).getBytes());

                                    VirtualFile settingsGradle = baseDir.createChildData(this, "settings.gradle");
                                    settingsGradle.setBinaryContent((gradleSettingsContent(projectName)).getBytes());

                                    VirtualFile litidata = baseDir.createChildData(this, "game.litidata");
                                    litidata.setBinaryContent(defaultLitiData().getBytes());

                                    if (selectedSdk != null) {
                                        ProjectRootManager.getInstance(project).setProjectSdk(selectedSdk);
                                    }

                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to create project structure", e);
                                }
                            });
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void setupUI(@NotNull Panel builder) {
                NewProjectWizardStep.super.setupUI(builder);

                List<Sdk> jdks = Arrays.stream(ProjectJdkTable.getInstance().getAllJdks())
                        .filter(sdk -> sdk.getSdkType() instanceof JavaSdk)
                        .sorted(Comparator.comparing(Sdk::getVersionString).reversed())
                        .toList();

                List<String> javaVersions = jdks.stream()
                        .map(Sdk::getVersionString)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

                String defaultVersion = javaVersions.isEmpty() ? "17" : javaVersions.get(0);
                GraphProperty<String> selectedVersion = getPropertyGraph().property(defaultVersion);

                builder.row("JDK:", row -> {
                    JComboBox<String> comboBox = new JComboBox<>(javaVersions.toArray(new String[0]));
                    comboBox.setSelectedItem(defaultVersion);
                    comboBox.setPreferredSize(new Dimension(300, comboBox.getPreferredSize().height));
                    row.cell(comboBox);

                    comboBox.addActionListener(e -> selectedVersion.set((String) comboBox.getSelectedItem()));

                    return Unit.INSTANCE;
                });

                builder.row("Package:", row -> {
                    JTextField textField = new JTextField();
                    textField.setText(packageNameProperty.get());
                    textField.setPreferredSize(new Dimension(300, textField.getPreferredSize().height));
                    row.cell(textField);

                    textField.getDocument().addDocumentListener(new DocumentAdapter() {
                        @Override
                        protected void textChanged(@NotNull DocumentEvent e) {
                            packageNameProperty.set(textField.getText().trim());
                        }
                    });

                    return Unit.INSTANCE;
                });
            }
        };
    }

    private VirtualFile createOrFindDirectory(VirtualFile base, String... segments) throws IOException {
        VirtualFile current = base;

        for (String segment : segments) {
            VirtualFile next = current.findChild(segment);
            System.out.println("Ensuring directory: " + current.getPath() + "/" + segment);

            if (next != null && !next.isDirectory()) {
                try {
                    next.delete(this);
                } catch (IOException e) {
                    throw new IOException("Failed to delete existing file: " + next.getPath(), e);
                }
                next = null;
            }

            if (next == null) {
                try {
                    next = current.createChildDirectory(this, segment);
                } catch (IOException e) {
                    throw new IOException("Failed to create directory: " + segment + " under " + current.getPath(), e);
                }
            }

            current = next;
        }

        return current;
    }

    private String buildMainClass(String projectName, String packageName) {
        return "package " + packageName + ";\n\n" +

                "import de.gurkenlabs.litiengine.*;\n" +
                "import de.gurkenlabs.litiengine.resources.Resources;\n\n" +

                "/**\n" +
                "*\n" +
                "* @see <a href=\"https://litiengine.com/docs/\">LITIENGINE Documentation</a>\n" +
                "*\n" +
                "*/\n\n" +

                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        // set meta information about the game\n" +
                "        Game.info().setName(\"" + projectName + "\");\n" +
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
                "        Resources.load(\"game.litidata\");\n\n" +

                "        // load the first level (resources for the map were implicitly loaded from the game file)\n" +
                "        // Game.world().loadEnvironment(\"path/to/level\");\n" +
                "        Game.start();\n" +
                "    }\n" +
                "}";
    }

    private String buildGradleContent(String packageName) {
        return "plugins {\n" +
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
                "    mainClass = '" + packageName + ".Main'\n" +
                "}";
    }

    private String gradleSettingsContent(String projectName){
        return "rootProject.name = '" + projectName + "'";
    }

    private String defaultLitiData() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<litidata version=\"1.0\">\n" +
                "<maps/>\n" +
                "<spriteSheets/>\n" +
                "<tilesets/>\n" +
                "<emitters/>\n" +
                "<blueprints/>\n" +
                "<sounds/>\n" +
                "</litidata>\n";
    }

}
