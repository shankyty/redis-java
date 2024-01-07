package com.github.shankyty.redis;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.List;

public class Main {

    public static final int CAPACITY = 1_024;
    public static final String RESP_DELIMITER = "\r\n";
    public static final String ARRAY_TOKEN = "*";
    public static final String BULK_STRING_TOKEN = "$";
    private static final String SIMPLE_STRING_TOKEN = "+";
    private static final String ERROR_TOKEN = "-";

    public static void main(String[] args) throws IOException {
        int port = 6379;
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
        for (int i = 0; i < args.length; i++) {
            switch (args[i]){
                case "--dir":
                case "--dbfilename":
                    new CommandImpl(CommandType.config, Arrays.asList(args[i],args[i + 1])).getResponse();
                    i++;
                default:
            }
        }
        while (true) {
            InMemoryStore.getInstance().cleanup();
            if(selector.selectNow() > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        register(selector, serverSocket, key);
                    } else if (key.isReadable()) {
                        read(buffer, key);
                    } else if (key.isWritable()) {
                        write(key);
                    }
                    iter.remove();
                }
            }
        }
    }

    private static void register(Selector selector,
                                 ServerSocketChannel serverSocket,
                                 SelectionKey key) throws IOException {
        System.out.println("accepting key = " + key);
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private static void read(ByteBuffer buffer,
                             SelectionKey key) throws IOException {
        System.out.println("reading key = " + key);
        SocketChannel client = (SocketChannel) key.channel();
        if(client.isOpen()) {
            StringBuilder sb = new StringBuilder();
            try {
                int c = client.read(buffer);
                while (c > 0) {
                    System.out.println("c = " + c);
                    sb.append(new String(buffer.array(), 0, buffer.position()));
                    buffer.clear();
                    c = client.read(buffer);
                }
                if (c == -1) {
                    // Remote entity shut the socket down cleanly. Do the
                    // same from our end and cancel the channel.
                    System.out.println("Client closed connection.");
                    client.close();
                    key.cancel();
                } else {
                    System.out.println("Reading buffer = " + sb.toString());
                    String commandStr = (String) key.attachment();

                    if(Objects.nonNull(commandStr))
                        commandStr = commandStr + sb.toString();
                    else {
                        commandStr = sb.toString();
                    }
                    Command command = parseResp(commandStr);
                    System.out.println("read = " + command);
                    key.attach(command);
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            } catch (IOException ex) {
                System.out.println("Canceling key and closing connection" + ex.getMessage());
                ex.printStackTrace(System.out);
                key.cancel();
                client.close();
            }
        } else{
            System.out.println("Canceling key");
            key.cancel();
        }
    }

    private static Command parseResp(String commandStr) {
        String[] commands = commandStr.split(RESP_DELIMITER);
        int len = commands.length;
        if(len < 1){
            return new CommandImpl(CommandType.unknown, Arrays.asList(commands));
        }
        int offest = 0;
        if(commands[0].charAt(0) == '*')
            offest = 2;
        else
            offest = 1;
        String commandKey = commands[offest];
        offest+=2;
        List<String> args = new ArrayList<>();
        for(int i  = offest; i < len; i+=2){
            args.add(commands[i]);
        }
        return new CommandFactory().getCommand(commandKey, args);
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            System.out.println("writing key = " + key);
            Command command = (Command) key.attachment();
            System.out.println("command = " + command);
            if (CommandType.unknown.equals(command.getType())) {
                writeError(client, command.getResponse());
            } else{
                writeRespResponse(client, command.getResponse());
            }
            key.attach(null);
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException ex) {
            System.out.println("Canceling key and closing connection" + ex.getMessage());
            ex.printStackTrace(System.out);
            key.cancel();
            client.close();
        }
    }

    private static void writeRespResponse(SocketChannel client, List<String> response) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(response.size() > 1) {
            writeBulkStrings(response, sb);
        } else if(response.size() == 1){
            writeSimpleString(response.get(0), sb);
        } else {
            writeEmptyString(sb);
        }
        System.out.println("response = " + sb);
        client.write(ByteBuffer.wrap(sb.toString().getBytes()));
    }

    private static void writeEmptyString(StringBuilder sb) {
         sb.append(BULK_STRING_TOKEN)
                .append(0)
                .append(RESP_DELIMITER)
                .append(RESP_DELIMITER);
    }

    private static void writeNullString(StringBuilder sb) {
        sb.append(BULK_STRING_TOKEN)
                .append(-1)
                .append(RESP_DELIMITER);
    }
    private static void writeError(SocketChannel client, List<String> response) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(ERROR_TOKEN);
        response.forEach(__ -> sb.append(__).append(" "));
        sb.append(RESP_DELIMITER);
        System.out.println("response = " + sb);
        client.write(ByteBuffer.wrap(sb.toString().getBytes()));
    }

    private static StringBuilder writeSimpleString(String response, StringBuilder sb) {
        if(response == null){
            writeNullString(sb);
        } else if (response.isEmpty()) {
            writeEmptyString(sb);
        } else {
            sb.append(SIMPLE_STRING_TOKEN);
            sb.append(response)
                    .append(RESP_DELIMITER);
        }
        return sb;
    }

    private static StringBuilder writeBulkStrings(List<String> response, StringBuilder sb) {
        sb.append(ARRAY_TOKEN)
                .append(response.size())
                .append(RESP_DELIMITER);
        response.forEach(__ -> {
            if(__ == null){
                writeNullString(sb);
            } else if (__.isEmpty()) {
                writeEmptyString(sb);
            } else {
                writeBulkString(__, sb);
            }
        });
        return sb;
    }

    private static void writeBulkString(String value, StringBuilder sb) {
        sb.append(BULK_STRING_TOKEN)
                .append(value.length())
                .append(RESP_DELIMITER)
                .append(value)
                .append(RESP_DELIMITER);
    }

}
