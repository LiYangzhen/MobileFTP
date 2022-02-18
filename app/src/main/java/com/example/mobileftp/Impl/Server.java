package com.example.mobileftp.Impl;

import com.blankj.utilcode.util.PathUtils;
import com.example.mobileftp.Exceptions.PortUsingException;
import com.example.mobileftp.interfaces.ServerInterface;
import com.example.mobileftp.service.FtpConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Server implements ServerInterface {

    private int port = 12345;
    private String username = "admin";
    private String password = "admin";
    private String root = FtpConfig.SAVE_FILE_PATH;

    private ServerSocket serverSocket;
    private String type = "binary";
    private List<Thread> threads = new ArrayList<>();

    public Server() {
        init();
    }

    public Server(int port, String username, String password) throws IOException {
        if (isPortUsing(port))
            throw new PortUsingException(port);
        this.port = port;
        this.username = username;
        this.password = password;
        init();
    }

    private void init() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket PISocket = serverSocket.accept();
                new Thread(() -> {
                    serving(PISocket);
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void serving(Socket PISocket) {
        String index = Thread.currentThread().getName().split("-")[1];
        System.out.println("Get Client-" + index);
        String name = "Client-" + index + ": ";
        ServerSocket dataServerSocket = null;
        while (true) {
            try {
                String commend = accept_commend(PISocket);
                if (commend == null)
                    return;
                System.out.println(name + "get commend {" + commend + "}");
                String[] params = commend.split(" ");
                String msg = "OK";
                switch (params[0]) {
                    case "LOGIN":
                        msg = login(params[1], params[2]);
                        break;
                    case "QUIT":
                        quit();
                        send_reply(PISocket, msg);
                        if (dataServerSocket != null)
                            dataServerSocket.close();
                        System.out.println(name + "quit connection");
                        return;
                    case "PASV":
                        dataServerSocket = new ServerSocket(randomPort());
                        msg += " " + dataServerSocket.getLocalPort();
                        break;
                    case "PORT":
                        int port = Integer.parseInt(params[1]);
                        if (isPortUsing(port))
                            msg = "This port has been occupied";
                        else
                            dataServerSocket = new ServerSocket(port);
                        break;
                    case "STOR":
                        store(PISocket, dataServerSocket, params[1], Integer.parseInt(params[2]));
                        continue;
                    case "RETR":
                        retrieve(PISocket, dataServerSocket, params[1]);
                        continue;
                    case "TYPE":
                        msg = type(params[1]);
                        break;
                    case "LIST":
                        msg = getList();
                        break;
                }
                send_reply(PISocket, msg);
            } catch (IOException e) {
                e.printStackTrace();
                close_socket(PISocket, dataServerSocket);
                break;
            }
        }
    }

    private String getList() {
        File file = new File(root);
        StringBuilder list = new StringBuilder();
        if (file.list() != null) {
            for (String file_name : file.list()) {
                list.append(file_name).append(" ");
            }
        }
        return list.length() == 0 ? "" : list.substring(0, list.length() - 1);
    }

    private String accept_commend(Socket PISocket) throws IOException {
        InputStream inputStream = PISocket.getInputStream();
        byte[] buffer = new byte[2048];
        int len = 0;
        int pointer = 0;
        do {
            int curr_len = inputStream.read(buffer);
            if (curr_len == -1) {
                inputStream.close();
                PISocket.close();
                return null;
            }
            len += curr_len;
            if (curr_len != 0)
                pointer = len - 1;
        } while (buffer[pointer] != 10);
        return new String(buffer, 0, len - 1);
    }

    private void close_socket(Socket PISocket, ServerSocket dataServerSocket) {
        if (PISocket != null) {
            try {
                PISocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dataServerSocket != null) {
            try {
                dataServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void send_reply(Socket socket, String msg) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write((msg + '\n').getBytes());
    }

    private String login(String username, String password) {
        if (username.equals("anonymous") || (username.equals(this.username) && password.equals(this.password)))
            return "OK";
        return "Username or password is wrong";
    }

    private void quit() {
        for (Thread thread : threads) {
            if (thread.isAlive())
                thread.interrupt();
        }
    }

    private String type(String type) {
        if (type.equals("binary") || type.equals("ascii")) {
            this.type = type;
            return "OK";
        }
        return "No this type";
    }

    private void store(Socket PISocket, ServerSocket dataServerSocket, String path, int length) throws IOException {
        if (dataServerSocket == null) {
            send_reply(PISocket, "DTP has not initialized");
            return;
        }
        send_reply(PISocket, "OK");
        if (length < Client.MAX_LENGTH) {
            byte[] buffer = new byte[length];
            DTPThread thread1 = null;
            for (int i = 0; i < 2; i++) {
                DTPThread thread = new DTPThread(dataServerSocket.accept(), null, thread1);
                thread.setBuffer(buffer, path);
                threads.add(thread);
                thread1 = thread;
                thread.start();
            }
        } else {
            byte[] buffer = new byte[1024];
            DTPThread thread = new DTPThread(dataServerSocket.accept(), null, null);
            thread.setBuffer(buffer, path);
            threads.add(thread);
            thread.start();
        }
    }

    private void retrieve(Socket PISocket, ServerSocket dataServerSocket, String path) throws IOException {
        if (dataServerSocket == null) {
            send_reply(PISocket, "DTP has not initialized");
            return;
        }
//        File file = new File();
        path = FtpConfig.SAVE_FILE_PATH + "/" + path;
        File file = new File(path);
        if (!file.exists()) {
            send_reply(PISocket, "File not found: " + path);
            return;
        }
        send_reply(PISocket, "OK " + file.length());
        if (file.length() < Client.MAX_LENGTH) {
            DTPThread thread1 = null;
            for (int i = 0; i < 2; i++) {
                DTPThread thread = new DTPThread(dataServerSocket.accept(), null, thread1);
                thread.setFile(file, i + 1);
                threads.add(thread);
                thread1 = thread;
                thread.start();
            }
        } else {
            DTPThread thread = new DTPThread(dataServerSocket.accept(), null, null);
            thread.setFile(file, 0);
            threads.add(thread);
            thread.start();
        }
    }

    private int randomPort() {
        int port = 1024 + new Random().nextInt(65534);
        while (isPortUsing(port))
            port = 1024 + new Random().nextInt(65534);
        return port;
    }

    private boolean isPortUsing(int port) {
        try {
//            System.out.println(NetworkUtils.getIPAddress(true));
//            new Socket(InetAddress.getByName("127.0.0.1"), port);
            Socket s = new Socket();
            s.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
            s.close();
            return false;
        } catch (IOException e) {
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
//        Server server = new Server(21, "admin", "admin", "server");
//        server.run();
    }

}
