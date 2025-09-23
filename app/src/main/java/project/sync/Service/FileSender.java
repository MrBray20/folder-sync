package project.sync.Service;

import java.io.*;
import java.net.*;

public class FileSender {
    private String host;
    private int port;

    public FileSender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendFile(File file) throws IOException {
        try (Socket socket = new Socket(host, port);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF("FILE");
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[8192];
            int read;

            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
            dos.flush();
            System.out.println("File sent: " + file.getName());
        } catch (Exception e) {
            System.err.println("Failed to send file " + file.getName() + ": " + e.getMessage());
        }
        System.out.println("File sent: " + file.getName());
    }

    public void sendDeleteCommand(String fileName) {
        try (Socket socket = new Socket(host, port);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            dos.writeUTF("DELETE");
            dos.writeUTF(fileName);
            dos.flush();
            System.out.println("Delete command sent: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to send delete command for " + fileName + ": " + e.getMessage());
        }
    }
}
