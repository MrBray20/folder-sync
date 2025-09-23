package project.sync.Service;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FileReceiver {
    private int port;
    private final File destFolder;
    private static final Set<String> recentlySynced = Collections.synchronizedSet(new HashSet<>());

    public FileReceiver(int port, String destFolderPath) {
        this.port = port;
        this.destFolder = new File(destFolderPath);
        if (!this.destFolder.exists())
            this.destFolder.mkdirs();

    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Receuver listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                String command = dis.readUTF();

                if ("DELETE".equals(command)) {
                    String fileName = dis.readUTF();
                    File file = new File(destFolder, fileName);

                    if (file.exists()) {
                        boolean deleted = file.delete();
                        System.out.println("Delete received for " + fileName + " -> deleted: " + deleted);
                    } else {
                        System.out.println("Delete received for " + fileName + " -> file not found");
                    }
                } else if ("FILE".equals(command)) {
                    String fileName = dis.readUTF();
                    long fileSize = dis.readLong();

                    File file = new File(destFolder, fileName);

                    recentlySynced.add(file.getAbsolutePath());

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        long remaining = fileSize;

                        while ((read = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                            fos.write(buffer, 0, read);
                            remaining -= read;
                        }
                        fos.flush();
                    }
                    System.out.println("File received: " + file.getAbsolutePath());
                } else {
                    System.out.println("Unknown command received: " + command);
                }

            } catch (Exception e) {
                System.out.println("Receiver error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static boolean isRecentlySynced(File file) {
        return recentlySynced.remove(file.getAbsolutePath());
    }
}
