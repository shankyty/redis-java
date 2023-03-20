import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        //  Uncomment this block to pass the first stage
        int port = 6379;
        System.out.println("Opening port: " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            try (Socket clientSocket = serverSocket.accept()) {
                InputStream in = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()));

                String line;
                while ((line = reader.readLine()) != null) {

                    System.out.println("line = " + line);
                    if ("ping".equals(line)) {
                        writer.write("+PONG\r\n");
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
