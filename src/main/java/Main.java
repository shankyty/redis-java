import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        ExecutorService taskExecutor = null;
        try {
            taskExecutor = Executors.newCachedThreadPool(Executors
                    .defaultThreadFactory());
            // You can use print statements as follows for debugging, they'll be visible when running tests.
            System.out.println(namePrefix() + "Logs from your program will appear here!");
            //  Uncomment this block to pass the first stage
            int port = 6379;
            System.out.println(namePrefix() + "Opening port: " + port);
            try (ServerSocket serverSocket = new ServerSocket(port)) {

                serverSocket.setReuseAddress(true);
                // Wait for connection from client.
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    taskExecutor.submit(getReplyTask(clientSocket));
                }
            } catch (IOException e) {
                logException(e);
            }
        } finally {
            if(taskExecutor != null) {
                taskExecutor.shutdown();
                while (taskExecutor.isTerminated()) {

                }
            }
        }
    }
    public static Runnable getReplyTask(Socket clientSocket) {
        return () -> {
            try(AutoCloseable ignored = setupLogger(clientSocket)) {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {

                        System.out.println(namePrefix() + "line = " + line);
                        if ("ping".equals(line)) {
                            writer.write("+PONG\r\n");
                            writer.flush();
                        }
                    }
                } catch (IOException e) {
                    logException(e);
                } finally {
                    try {
                        System.out.println(namePrefix() + "closing");
                        clientSocket.close();
                    } catch (IOException e) {
                        logException(e);
                    }
                }
            } catch (Exception e) {
                logException(e);
            }
        };
    }

    private static void logException(Exception e) {
        System.out.println(namePrefix() + e.getClass().getSimpleName() + ": " + e.getMessage());
        e.printStackTrace(System.out);
    }

    private static void updateName(String name) {
        Thread.currentThread().setName(name);
    }

    private static AutoCloseable setupLogger(Socket clientSocket) {
        String oldName = Thread.currentThread().getName();
        updateName(clientSocket.getRemoteSocketAddress().toString());
        return () -> updateName(oldName);
    }

    private static String namePrefix(){
        return Thread.currentThread().getName() + ": ";
    }
}
