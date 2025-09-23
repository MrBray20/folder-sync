package project.sync.Service;

import java.io.*;
import java.net.*;

public class FileReceiver {
    private int port;
    private String destFolder;

    public FileReceiver(int port, String destFolder){
        this.port = port;
        this.destFolder = destFolder;

    }


    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Receuver listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();

                File file = new File(destFolder, fileName);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    long remaining = fileSize;

                    while ((read=dis.read(buffer, 0 , (int)Math.min(buffer.length, remaining))) > 0) {
                        remaining -= read;
                        fos.write(buffer,0,read);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
                System.out.println("File received: " + file.getAbsolutePath());
            } catch (Exception e) {
                // TODO: handle exception
            }
            socket.close();
        }
    }
}
