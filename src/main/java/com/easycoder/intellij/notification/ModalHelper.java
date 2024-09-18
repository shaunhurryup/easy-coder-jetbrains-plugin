package com.easycoder.intellij.notification;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import com.intellij.openapi.project.Project;

public class ModalHelper {
    public static void showInfo(Project project, String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    public static void showWarning(Project project, String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.WARNING_MESSAGE
            );
        });
    }

    public static void showError(Project project, String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
}