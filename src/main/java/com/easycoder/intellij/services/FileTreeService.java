package com.easycoder.intellij.services;

import com.easycoder.intellij.handlers.FileTreeManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

@Service(Service.Level.PROJECT)
public final class FileTreeService {
    private final FileTreeManager fileTreeManager;

    public FileTreeService(Project project) {
        fileTreeManager = new FileTreeManager(project);
        fileTreeManager.init();
    }

    public void dispose() {
        fileTreeManager.dispose();
    }
} 