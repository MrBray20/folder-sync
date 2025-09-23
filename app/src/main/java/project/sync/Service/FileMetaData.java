package project.sync.Service;

public class FileMetaData {
    public long lastModified;
    public long size;

    public FileMetaData(long lastModified, long size) {
        this.lastModified = lastModified;
        this.size = size;
    }
}
