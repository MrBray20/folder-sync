package project.sync.Service;
import java.io.File;
import java.nio.file.*;


public class FileWatcher {
    private Path folder;
    private FileSender sender;

    public FileWatcher(String foldePath, FileSender sender){
        this.folder = Paths.get(foldePath);
        this.sender = sender;
    }

    public void start() throws Exception{
        WatchService watchService = FileSystems.getDefault().newWatchService();
        folder.register(watchService, 
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY);

        System.out.println("Watching folder: " + folder);


        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                Path changed = folder.resolve((Path) event.context());
                File file = changed.toFile();

                if(file.exists() && file.isFile()) {
                    sender.sendFile(file);
                }
            }
            key.reset();
        }
    }
}
