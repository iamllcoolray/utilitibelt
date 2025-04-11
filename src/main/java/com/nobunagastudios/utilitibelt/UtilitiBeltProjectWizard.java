package com.nobunagastudios.utilitibelt;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard;
import com.intellij.openapi.observable.properties.PropertyGraph;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.ui.dsl.builder.Panel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
                NewProjectWizardStep.super.setupProject(project);
            }

            @Override
            public void setupUI(@NotNull Panel builder) {
                NewProjectWizardStep.super.setupUI(builder);
            }
        };
    }
}
