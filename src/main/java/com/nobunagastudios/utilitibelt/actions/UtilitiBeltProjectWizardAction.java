package com.nobunagastudios.utilitibelt.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class UtilitiBeltProjectWizardAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showInfoMessage(e.getProject(), "New LITIENGINE Project Wizard triggered!", "utiLITI Belt");
    }
}
