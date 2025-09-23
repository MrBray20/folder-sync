package project.sync.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MetaDataManger {
    private final File metadataFile;
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, FileMetaData> metadata = new HashMap<>();

    public MetaDataManger(String folderPath) {
        this.metadataFile = new File(folderPath, ".sync-state.json");
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        if (metadataFile.exists()) {
            try {
                metadata = mapper.readValue(metadataFile,
                        new TypeReference<Map<String, FileMetaData>>() {
                        });
            } catch (IOException e) {
                System.err.println("Failed to load metadata, starting freash: " + e.getMessage());
                metadata = new HashMap<>();
            }
        }
    }

    public void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(metadataFile, metadata);
        } catch (IOException e) {
            System.err.println("Failed to save metadata: " + e.getMessage());
        }
    }

    public Map<String, FileMetaData> getMetaData() {
        return metadata;
    }

    public void updateFile(String fileName, long lastModified, long size) {
        metadata.put(fileName, new FileMetaData(lastModified, size));
    }

    public void removeFile(String fileName) {
        metadata.remove(fileName);
    }
}
