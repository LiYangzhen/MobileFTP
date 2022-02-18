package com.example.mobileftp.Impl;

import com.blankj.utilcode.util.FileIOUtils;
import com.example.mobileftp.service.FtpConfig;

import java.io.*;
import java.net.Socket;

public class DTPThread extends Thread {

    private Socket socket;
    private Listener listener;
    private File file;
    private int length;
    private byte[] buffer;
    private String path;
    private int partition;
    private Thread subThread;
    private long startTime = System.currentTimeMillis();

    public DTPThread(Socket socket, Listener listener, Thread subThread) {
        this.socket = socket;
        this.listener = listener;
        this.subThread = subThread;
        setName("DTP-" + getName().split("-")[1]);
    }

    public void setFile(File file, int partition) {
        this.file = file;
        this.partition = partition;
        this.length = (int) file.length();
        int half = length / 2;
        if (partition == 1)
            length = half;
        else if (partition == 2)
            length -= half;
    }

    public void setBuffer(byte[] buffer, String path) {
        this.buffer = buffer;
        this.path = FtpConfig.SAVE_FILE_PATH + "/" + path;
        this.length = buffer.length;
    }

    @Override
    public void run() {
        if (file != null)
            send();
        else
            accept();
        if (!isInterrupted() && subThread != null) {
            try {
                subThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (partition == 2 && buffer != null) {
            try {
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
                outputStream.write(buffer);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//                FileIOUtils.writeFileFromBytesByChannel(FtpConfig.SAVE_FILE_PATH + "/" + path, buffer, false);
        if (partition != 1 && listener != null && listener.getValue().length() < 1)
                listener.setValue(String.format("OK %.2fM/s", getSpeed()));
    }

    private void send() {
        InputStream inputStream = null;
        try {
            byte[] buffer;
            if (partition == 0)
                buffer = new byte[1024];
            else
                 buffer = new byte[length];

            inputStream = new BufferedInputStream(new FileInputStream(file));
            OutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(partition);
            if (partition == 0) {
                int len;
                while (!isInterrupted() && (len = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, len);
            } else {
                if (!isInterrupted()) {
                    if (partition == 2)
                        inputStream.read(new byte[(int) file.length() / 2]);
                    inputStream.read(buffer);
                    outputStream.write(buffer);
                }
            }
            if (isInterrupted())
                throw new InterruptedIOException("IO interrupted");
            outputStream.close();
        } catch (IOException e) {
            reportException(e);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept() {
        try {
            InputStream inputStream = new BufferedInputStream(socket.getInputStream());
            int half = buffer.length / 2;
            this.partition = inputStream.read();
            int sum = buffer.length;
            int off = 0;
            if (partition == 1) {
                sum = half;
            } else if (partition == 2) {
                off = half;
                sum -= half;
            }
            int len = 0;
            if (partition == 0) {
                length = 0;
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(path));
                while (!isInterrupted() && (len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                    length += len;
                }
                outputStream.close();

            } else {
                while (!isInterrupted() && len < sum) {
                    int a = inputStream.read(buffer, off, sum - len);
                    if (a == -1)
                        break;
                    len += a;
                    off += a;
                }
            }
            if (isInterrupted())
                throw new InterruptedIOException("IO interrupted");
            inputStream.close();
        } catch (IOException e) {
            reportException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double getSpeed() {
//        if (file == null)
//            return buffer.length * 1.0 / (System.currentTimeMillis() - startTime) / 1000;
//        else
//            return file.length() * 1.0 / (System.currentTimeMillis() - startTime) / 1000;
        return length * 1.0 / (System.currentTimeMillis() - startTime) / 1000;
    }

    private void reportException(Exception e) {
        if (listener != null) {
            e.printStackTrace();
            listener.setValue(e.getMessage());
        } else {
            e.printStackTrace();
        }
    }
}
