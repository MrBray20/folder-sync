package project.sync.Service;

import java.io.*;
import java.net.*;

public class FileSender {
    private String host;
    private int port;

    public FileSender(String host, int port){
        this.host = host;
        this.port = port;
    }

    public void sendFile(File file) throws IOException {
        try (Socket socket = new Socket(host, port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(file)) {
            
            
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int read;

            while ((read = fis.read(buffer)) > 0) {
                dos.write(buffer,0,read);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        System.out.println("File sent: " + file.getName());
    }
}
