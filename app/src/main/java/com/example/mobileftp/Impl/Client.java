package com.example.mobileftp.Impl;


import com.example.mobileftp.Exceptions.NotInitException;
import com.example.mobileftp.Exceptions.ServerBreakDownException;
import com.example.mobileftp.interfaces.ClientInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements ClientInterface {


    public static final int MAX_LENGTH = 100 * 1024 * 1024;

    private InetAddress DATA_HOST;
    private Socket PISocket;
    private Integer data_port;
    private String type = "binary";
    private List<Thread> threads = new ArrayList<>();

    @Override
    public String connect(String username, String password, String IP, int port) throws IOException{
        InetAddress inetAddress = InetAddress.getByName(IP);
        PISocket = new Socket(inetAddress, port);
        String msg = send_commend(String.format("LOGIN %s %s\n", username, password));
        if (!msg.startsWith("OK"))
            quit();
        DATA_HOST = inetAddress;
        return msg;
    }

    @Override
    public String quit() {
        try {
            for (Thread thread: threads) {
                if (thread.isAlive()) {
                    thread.interrupt();
                    System.out.println(thread.getName() + " close");
                }
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String msg = send_commend("QUIT\n");
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                PISocket.shutdownOutput();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            try {
                PISocket.shutdownInput();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            try {
                PISocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return "OK";
    }

    @Override
    public String port(int port) throws IOException{
        data_port = port;
        return send_commend("PORT " + port + "\n");
    }

    @Override
    public String passive() throws IOException{
        String msg = send_commend("PASV\n");
        String[] server_info = msg.split(" ");
        data_port = Integer.parseInt(server_info[1]);
        return msg;
    }

    @Override
    public String type(String type) throws IOException {
        if (type.equals("binary") || type.equals("ascii")) {
            this.type = type;
            return send_commend("TYPE " + type + "\n");
        }
        return "No this type";
    }

    @Override
    public String store(String path, File file, Listener listener) throws IOException {
        if (data_port == null)
            throw new NotInitException();
        if (path == null)
            path = file.getName();
        String msg = send_commend(String.format("STOR %s %d\n", path, file.length()));
        if (file.length() < MAX_LENGTH) {
            DTPThread thread1 = null;
            for (int i = 0; i < 2; i++) {
                DTPThread thread = new DTPThread(new Socket(DATA_HOST, data_port), listener, thread1);
                thread.setFile(file, i + 1);
                threads.add(thread);
                thread1 = thread;
                thread.start();
            }
        } else {
            DTPThread thread = new DTPThread(new Socket(DATA_HOST, data_port), listener, null);
            thread.setFile(file, 0);
            threads.add(thread);
            thread.start();
        }
        return msg;
    }

    @Override
    public String retrieve(String path, String copy_path, Listener listener) throws IOException{
        if (data_port == null)
            throw new NotInitException();
        if (copy_path == null)
            copy_path = path;
        String msg = send_commend("RETR " + path + "\n");
        int length = Integer.parseInt(msg.split(" ")[1]);
        if (length < MAX_LENGTH) {
            DTPThread thread1 = null;
            byte[] buffer = new byte[length];
            for (int i = 0; i < 2; i++) {
                DTPThread thread = new DTPThread(new Socket(DATA_HOST, data_port), listener, thread1);
                thread.setBuffer(buffer, copy_path);
                threads.add(thread);
                thread1 = thread;
                thread.start();
            }
        } else {
            byte[] buffer = new byte[1024];
            DTPThread thread = new DTPThread(new Socket(DATA_HOST, data_port), listener, null);
            thread.setBuffer(buffer, copy_path);
            threads.add(thread);
            thread.start();
        }
        return msg;
    }

    @Override
    public String mode(String mode) throws IOException {
        return send_commend("MODE " + mode + "\n");
    }

    @Override
    public String structure(String structure) throws IOException {
        return send_commend("STRU " + structure + "\n");
    }

    @Override
    public String noop() throws IOException {
        return send_commend("NOOP\n");
    }

    @Override
    public String[] getFileList() throws IOException {
        return send_commend("LIST\n").split(" ");
    }

    private String send_commend(String commend) throws IOException{
        OutputStream outputStream = PISocket.getOutputStream();
        outputStream.write(commend.getBytes());
        InputStream inputStream = PISocket.getInputStream();
        byte[] buffer = new byte[2048];
        int len = 0;
        int pointer = 0;
        do {
            int curr_len = inputStream.read(buffer);
            if (curr_len == -1)
                throw new ServerBreakDownException();
            len += curr_len;
            if (curr_len != 0)
                pointer = len - 1;
        } while (buffer[pointer] != 10);
        return new String(buffer, 0, len - 1);
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {

            Listener listener = new Listener("");
            listener.setCallBack(value -> {
                System.out.println(value);
            });

            System.out.println(client.connect("admin", "admin", "127.0.0.1", 21));
            client.port(661);
            client.store("big0000", new File("client/big0000"), listener);
            Thread.sleep(10000);
            System.out.println(client.quit());
        } catch (IOException | InterruptedException e) {
            client.quit();
            e.printStackTrace();
        }
    }

}
