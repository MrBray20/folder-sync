package project.sync.Service;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {
    private final Path folder;
    private final FileSender sender;
    private final MetaDataManger metaDataManger;

    public FileWatcher(String foldePath, FileSender sender) {
        this.folder = Paths.get(foldePath);
        this.sender = sender;
        this.metaDataManger = new MetaDataManger(foldePath);
    }

    private void scanAndSync() throws Exception {
        Map<String, FileMetaData> stored = metaDataManger.getMetaData();
        Set<String> currentFiles = new HashSet<>();

        File[] files = folder.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && !f.getName().equals(".sync-satate.json")) {
                    currentFiles.add(f.getName());

                    long lastModified = f.lastModified();
                    long size = f.length();

                    FileMetaData oldMeta = stored.get(f.getName());
                    if (oldMeta == null || oldMeta.lastModified != lastModified || oldMeta.size != size) {
                        sender.sendFile(f);
                        metaDataManger.updateFile(f.getName(), lastModified, size);
                        System.out.println("Synced (startup/rescan): " + f.getName());
                    }
                }
            }
        }

        for (String fileName : new HashSet<>(stored.keySet())) {
            if (!currentFiles.contains(fileName)) {
                sender.sendDeleteCommand(fileName);
                metaDataManger.removeFile(fileName);
                System.out.println("Deleted (startup/rescan): " + fileName);
            }
        }

        metaDataManger.save();

    }

    public void start() throws Exception {

        scanAndSync();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                scanAndSync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 60, 60, TimeUnit.SECONDS);

        WatchService watchService = FileSystems.getDefault().newWatchService();
        folder.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

        System.out.println("Watching folder: " + folder);

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path context = (Path) event.context();
                Path absolutePath = folder.resolve(context);
                File file = absolutePath.toFile();

                if (kind == ENTRY_CREATE) {
                    if (FileReceiver.isRecentlySynced(file)) {
                        System.out.println("Skipping created file (recently synced): " + file.getName());
                        continue;
                    }

                    if (file.exists() && file.isFile()) {
                        System.out.println("Detected CREATE for: " + file.getName());
                        sender.sendFile(file);
                    }
                } else if (kind == ENTRY_MODIFY) {
                    if (FileReceiver.isRecentlySynced(file)) {
                        System.out.println("Skipping modified file (recently synced): " + file.getName());
                        continue;
                    }
                    if (file.exists() && file.isFile()) {
                        System.out.println("Detected MODIFY for: " + file.getName());
                        sender.sendFile(file);
                    }
                } else if (kind == ENTRY_DELETE) {
                    String deletedName = context.toString();
                    System.out.println("Detected DELETE for: " + deletedName);
                    sender.sendDeleteCommand(deletedName);
                } else {
                    System.out.println("Other event: " + kind.name());
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                System.out.println("WatchKey no longer valid, exiting watcher");
                break;
            }
        }
    }
}
