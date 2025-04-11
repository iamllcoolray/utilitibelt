package com.nobunagastudios.utilitibelt;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class UtilitiBeltProjectAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Messages.showInfoMessage("New LITIENGINE Project Wizard triggered!", "utiLITI Belt Project Wizard");
    }
}
