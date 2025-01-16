package com.easycoder.intellij.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;

public class FileTreeManager {
    private final Project project;
    private MessageBusConnection connection;

    public FileTreeManager(Project project) {
        this.project = project;
    }

    /**
     * Initialize file tree and set up file system listeners
     */
    public void init() {
        // Wait for indexing to complete before initializing
        DumbService.getInstance(project).runWhenSmart(() -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                initFileTree();
                initFileTreeWatcher();
            });
        });
    }

    /**
     * Initialize file tree by scanning project directory
     */
    private void initFileTree() {
        if (project.getBasePath() == null) {
            return;
        }

        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(project.getBasePath());
        if (baseDir == null) {
            return;
        }

        List<String> gitignorePatterns = readGitignore(project.getBasePath());
        List<FileTreeItem> fileTree = buildFileTree(baseDir, "", gitignorePatterns);

        JsonArray files = new JsonArray();
        for (FileTreeItem item : fileTree) {
            JsonObject fileItem = new JsonObject();
            fileItem.addProperty("type", item.type);
            fileItem.addProperty("value", item.value);
            fileItem.addProperty("action", "add");
            files.add(fileItem);
        }

        JsonObject payload = new JsonObject();
        payload.add("$array", files);

        WebviewMessage message = WebviewMessage.builder()
                .id(MessageId.SyncFileTree)
                .payload(payload)
                .build();

        project.getService(EasyCoderSideWindowService.class)
                .notifyIdeAppInstance(new Gson().toJson(message));
    }

    /**
     * Initialize file system watcher
     */
    private void initFileTreeWatcher() {
        if (connection != null) {
            connection.disconnect();
        }

        connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                // Only process events when indexing is complete
                DumbService.getInstance(project).runWhenSmart(() -> {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        for (VFileEvent event : events) {
                            handleFileEvent(event);
                        }
                    });
                });
            }
        });
    }

    /**
     * Handle file system events
     */
    private void handleFileEvent(VFileEvent event) {
        if (event.getFile() == null || !isInProjectDirectory(event.getFile())) {
            return;
        }

        String relativePath = VfsUtil.getRelativePath(event.getFile(), project.getBaseDir());
        if (relativePath == null) {
            return;
        }

        boolean isDirectory = event.getFile().isDirectory();
        String action = null;
        if (event instanceof VFileCreateEvent) {
            action = "add";
        } else if (event instanceof VFileDeleteEvent) {
            action = "delete";
        } else {
            return;
        }

        JsonArray files = new JsonArray();
        JsonObject fileItem = new JsonObject();
        fileItem.addProperty("type", isDirectory ? "directory" : "file");
        fileItem.addProperty("value", isDirectory ? relativePath + "/" : relativePath);
        fileItem.addProperty("action", action);
        files.add(fileItem);

        JsonObject payload = new JsonObject();
        payload.add("$array", files);

        WebviewMessage message = WebviewMessage.builder()
                .id(MessageId.SyncFileTree)
                .payload(payload)
                .build();

        project.getService(EasyCoderSideWindowService.class)
                .notifyIdeAppInstance(new Gson().toJson(message));
    }

    /**
     * Check if file is in project directory
     */
    private boolean isInProjectDirectory(VirtualFile file) {
        if (project.getBasePath() == null) {
            return false;
        }
        String filePath = file.getPath();
        String projectPath = project.getBasePath();
        return filePath.startsWith(projectPath);
    }

    /**
     * Read .gitignore patterns
     */
    private List<String> readGitignore(String rootPath) {
        List<String> patterns = new ArrayList<>();
        File gitignore = new File(rootPath, ".gitignore");
        
        if (!gitignore.exists()) {
            return patterns;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(gitignore))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    patterns.add(line);
                }
            }
        } catch (IOException e) {
            // Ignore errors reading .gitignore
        }

        return patterns;
    }

    /**
     * Build file tree recursively
     */
    private List<FileTreeItem> buildFileTree(VirtualFile root, String relativePath, List<String> gitignorePatterns) {
        List<FileTreeItem> result = new ArrayList<>();
        
        for (VirtualFile child : root.getChildren()) {
            String name = child.getName();
            if (name.startsWith(".")) {
                continue;
            }

            String childPath = relativePath.isEmpty() ? name : relativePath + "/" + name;
            
            if (isIgnored(childPath, gitignorePatterns)) {
                continue;
            }

            if (child.isDirectory()) {
                result.add(new FileTreeItem("directory", childPath + "/"));
                result.addAll(buildFileTree(child, childPath, gitignorePatterns));
            } else {
                result.add(new FileTreeItem("file", childPath));
            }
        }

        return result;
    }

    /**
     * Check if path matches gitignore patterns
     */
    private boolean isIgnored(String path, List<String> gitignorePatterns) {
        String normalizedPath = path.replace('\\', '/');
        
        for (String pattern : gitignorePatterns) {
            pattern = pattern.trim().replace("\\", "/");
            if (pattern.isEmpty()) continue;

            // Convert glob pattern to regex
            String regex = pattern
                    .replace(".", "\\.")
                    .replace("**/", ".*")
                    .replace("*", "[^/]*")
                    .replace("?", ".");

            if (Pattern.matches(regex, normalizedPath)) {
                return true;
            }
        }
        
        return false;
    }

    private static class FileTreeItem {
        String type;
        String value;

        FileTreeItem(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    public void dispose() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
} 