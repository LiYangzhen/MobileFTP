package com.example.mobileftp.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import com.blankj.utilcode.util.UriUtils;
import com.example.mobileftp.Impl.Client;
import com.example.mobileftp.Impl.Listener;
import com.example.mobileftp.service.FtpConfig;
import com.example.mobileftp.R;
import com.example.mobileftp.databinding.ActivityClientBinding;

import java.io.File;
import java.io.IOException;

public class ClientActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FtpClientActivity";
    private ActivityClientBinding binding;
    private static final int REQUEST_CHOOSE_FILE = 100;
    private Client client;
    private int passive = 0;
    private int encode = 0;
    private int transmission = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_client);
        initView();
//        verifyStoragePermissions(this);
        System.out.println(FtpConfig.SAVE_FILE_PATH);
    }

    private void initView() {
        binding.btnConnect.setOnClickListener(this);
        binding.btnDisconnect.setOnClickListener(this);
        binding.btnUploadFile.setOnClickListener(this);
        binding.btnDownloadFile.setOnClickListener(this);
        binding.btEncode.setOnClickListener(this);
        binding.btFileStruction.setOnClickListener(this);
        binding.btPassive.setOnClickListener(this);
        binding.btTransmission.setOnClickListener(this);

        binding.etServerPort.setText(FtpConfig.DEFAULT_PORT + "");
        binding.etUsername.setText(FtpConfig.DEFAULT_USER);
        binding.etPassword.setText(FtpConfig.DEFAULT_PASSWORD);
        binding.etServerIp.setText("192.168.31.146");
        binding.etPassivePort.setText(FtpConfig.DEFAULT_PASSIVE_PORT.toString());
        binding.tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    private void passiveDialogShowRadio() {
        String[] s = new String[]{"主动模式", "被动模式"};
        new AlertDialog.Builder(this)
                .setTitle("主动/被动模式")
                .setSingleChoiceItems(s, passive, (dialog, which) -> {
                    if (client != null) {
                        switch (which) {
                            case 0:
                                passive = 0;
                                int i = Integer.parseInt(binding.etPassivePort.getText().toString());
                                new Thread(() -> {
                                    try {
                                        client.port(i);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            case 1:
                                passive = 1;
                                new Thread(() -> {
                                    try {
                                        client.passive();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void typeDialogShowRadio() {
        String[] s = new String[]{"ASCII", "Binary"};
        new AlertDialog.Builder(this)
                .setTitle("编码方式")
                .setSingleChoiceItems(s, encode, (dialog, which) -> {
                    if (client != null) {

                        switch (which) {
                            case 0:
                                encode = 0;
                                new Thread(() -> {
                                    try {
                                        client.type("ASCII");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            case 1:
                                encode = 1;
                                new Thread(() -> {
                                    try {
                                        client.type("Binary");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                        }
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void modeDialogShowRadio() {
        String[] s = new String[]{"Stream", "Block", "Compressed"};
        new AlertDialog.Builder(this)
                .setTitle("传输方式")
                .setSingleChoiceItems(s, transmission, (dialog, which) -> {
                    if (client != null) {
                        switch (which) {
                            case 0:
                                transmission = 0;
                                new Thread(() -> {
                                    try {
                                        client.mode("Stream");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            case 1:
                                transmission = 1;
                                new Thread(() -> {
                                    try {
                                        client.mode("Block");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                            case 2:
                                transmission = 2;
                                new Thread(() -> {
                                    try {
                                        client.mode("Compressed");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }).start();
                                break;
                        }
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void fileStructureDialogShowInput() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("文件结构");
        dialog.setView(new EditText(this));
        dialog.setPositiveButton("确定", null);
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                new Thread(this::connectFtpServer).start();
                break;
            case R.id.btn_disconnect:
                new Thread(this::disconnectFtpServer).start();
                break;
            case R.id.btn_upload_file:
                uploadFile();
                break;
            case R.id.btn_download_file:
                new Thread(this::storeDialogShowList).start();
                break;
            case R.id.bt_encode:
                typeDialogShowRadio();
                break;
            case R.id.bt_file_struction:
                fileStructureDialogShowInput();
                break;
            case R.id.bt_passive:
                passiveDialogShowRadio();
                break;
            case R.id.bt_transmission:
                modeDialogShowRadio();
                break;
            default:
                break;
        }
    }

    private void uploadFile() {
        if (client != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_CHOOSE_FILE);
        } else {
            updateOutputMsg("Connection is not established");
        }
    }

    private void storeDialogShowList() {
        if (client != null) {
            String[] files;
            try {
                files = client.getFileList();
                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientActivity.this);
                    builder.setTitle("选择文件下载");
                    builder.setItems(files, (dialog, which) -> {
                        Listener listener = new Listener("");
                        listener.setCallBack(value -> {
                            if (value.startsWith("OK")) {
                                updateOutputMsg(files[which] + "OK,speed:" + value.substring(3));
                            } else {
                                updateOutputMsg(files[which] + "Download failure:" + value);
                                System.out.println(value);
                            }
                        });
                        new Thread(() -> {
                            try {
                                File file = new File(FtpConfig.SAVE_FILE_PATH + "/Download");
                                if (!file.exists()) {
                                    file.mkdir();
                                }
                                client.retrieve(files[which], "Download/" + files[which], listener);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                    builder.show();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            updateOutputMsg("Connection is not established");
        }
    }

    private void disconnectFtpServer() {
        if (client != null) {

            client.quit();
            client = null;
            updateOutputMsg("FtpServer has been disconnected");
        } else {
            updateOutputMsg("Connection is not established");
        }
    }

    private void connectFtpServer() {
        if (client == null) {
            final String serverIp = binding.etServerIp.getText().toString();
            final String serverPort = binding.etServerPort.getText().toString();
            final String username = binding.etUsername.getText().toString();
            final String password = binding.etPassword.getText().toString();

            try {
                client = new Client();
                String msg = client.connect(username, password, serverIp, Integer.parseInt(serverPort));
                if (msg.startsWith("OK")) {
                    updateOutputMsg("Login username:" + username);
                    updateOutputMsg("Login password:" + password);
                    updateOutputMsg("Connecting to server[" + serverIp + ":" + serverPort + "]");
                    updateOutputMsg("Download fails is in " + FtpConfig.SAVE_FILE_PATH);
                    updateOutputMsg("Connect success!");
                } else {
                    updateOutputMsg("Connect Fail,Err:" + msg);
                }
                int i = Integer.parseInt(binding.etPassivePort.getText().toString());
                new Thread(() -> {
                    try {
                        client.port(i);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (Exception e) {
                updateOutputMsg("Connect Fail,Err:" + e.getMessage());
                client = null;
            }
        } else {
            updateOutputMsg("Connection has been established,do not connect twice");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_FILE) {
            if (data != null && data.getData() != null && client != null) {
                File file = UriUtils.uri2File(data.getData());
                Listener listener = new Listener("");
                listener.setCallBack(value -> {
                    if (value.startsWith("OK")) {
                        updateOutputMsg(file.getName() + "OK,speed:" + value.substring(3));
                    } else {
                        updateOutputMsg(file.getName() + "Upload failure:" + value);
                        System.out.println(value);
                    }
                });

                new Thread(() -> {
                    try {
                        client.store(file.getName(), file, listener);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private void updateOutputMsg(String msg) {
        runOnUiThread(() -> {
            final String oldMsg = binding.tvMsg.getText().toString();
            binding.tvMsg.setText(oldMsg + "\n" + msg);
        });

    }
}