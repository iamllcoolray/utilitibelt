package com.hibiscusgames.utilitibelt;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.observable.properties.GraphProperty;
import com.intellij.openapi.observable.properties.PropertyGraph;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.dsl.builder.Panel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class UtilitiBeltProjectWizard implements LanguageGeneratorNewProjectWizard {

    private static final Configuration FREEMARKER_CFG;

    static {
        FREEMARKER_CFG = new Configuration(Configuration.VERSION_2_3_32);
        FREEMARKER_CFG.setClassLoaderForTemplateLoading(
                UtilitiBeltProjectWizard.class.getClassLoader(),
                "templates"
        );
        FREEMARKER_CFG.setDefaultEncoding("UTF-8");
    }

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

                        Map<String, Object> model = new HashMap<>();
                        model.put("projectName", projectName);
                        model.put("packageName", packageName);

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

                                    writeTemplate(packageDir, "Main.java",      "Main.java.ftl",       model);
                                    writeTemplate(baseDir,    "build.gradle",   "build.gradle.ftl",    model);
                                    writeTemplate(baseDir,    "settings.gradle","settings.gradle.ftl", model);
                                    writeTemplate(baseDir,    "game.litidata",  "game.litidata.ftl",   model);

                                    if (selectedSdk != null) {
                                        ProjectRootManager.getInstance(project).setProjectSdk(selectedSdk);
                                    }

                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to create project structure", e);
                                }
                            });
                        });

                    } catch (ProcessCanceledException e) {
                        throw e;
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

    private void writeTemplate(VirtualFile dir, String fileName,
                               String templateName, Map<String, Object> model) throws IOException {
        try {
            Template template = FREEMARKER_CFG.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            VirtualFile file = dir.createChildData(this, fileName);
            file.setBinaryContent(writer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (TemplateException e) {
            throw new IOException("Template processing failed for: " + templateName, e);
        }
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
}
