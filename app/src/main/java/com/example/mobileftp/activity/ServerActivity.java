package com.example.mobileftp.activity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.databinding.DataBindingUtil;
import com.blankj.utilcode.util.NetworkUtils;
import com.example.mobileftp.Impl.Server;
import com.example.mobileftp.service.FtpConfig;
import com.example.mobileftp.R;
import com.example.mobileftp.databinding.ActivityServerBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ServerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FtpServerActivity";
    private ActivityServerBinding binding;
    private Server server;
    private Thread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_server);
        initData();
//        verifyStoragePermissions(this);
    }

    private void initData() {
        binding.btnStartServer.setOnClickListener(this);
        binding.btnStopServer.setOnClickListener(this);

        binding.tvMsg.setMovementMethod(ScrollingMovementMethod.getInstance());
        binding.seUsername.setText(FtpConfig.DEFAULT_USER);
        binding.seUserPassword.setText(FtpConfig.DEFAULT_PASSWORD);
        binding.sePort.setText(FtpConfig.DEFAULT_PORT.toString());

        uploadMsg("Hotspot is opened ? " + isWifiApOpen(this));
    }

    private void uploadMsg(String msg) {
        final String oldMsg = binding.tvMsg.getText().toString();
        binding.tvMsg.setText(oldMsg + "\n" + msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_server:
                startFtpServer(binding.seUsername.getText().toString(),
                        binding.seUserPassword.getText().toString(),
                        "",
                        Integer.parseInt(binding.sePort.getText().toString()));
                break;
            case R.id.btn_stop_server:
                stopFtpServer();
                break;
        }
    }

    public void startFtpServer(String name, String pw, String sharePath, int port) {
        Log.d(TAG, "startFtpServer: " + Thread.currentThread().getName());
        try {
            if (server == null) {
                server = new Server(port, name, pw);
                serverThread = new Thread(() -> {
                    server.run();
                });
                serverThread.start();


                String serverIp = NetworkUtils.getIPAddress(true) + ":" + port;
                uploadMsg(
                        "username=" + name + "\n"
                                + "pw=" + pw + "\n"
                                + "share path=" + FtpConfig.SAVE_FILE_PATH + "\n"
                                + "serverIp=" + serverIp + "\n\n"
                                + "Ftp Server is running!" + "\n"
                                + "1.Browser open url: ftp://" + serverIp + "\n"
                                + "2.Use this or other ftp client connect server\n");
            } else {
                uploadMsg("FtpServer had been started");
            }
        } catch (Exception e) {
            server = null;
            uploadMsg("FtpServer failed to start");
            e.printStackTrace();
        }

    }

    public void stopFtpServer() {
        if (server != null) {
            serverThread.interrupt();
            serverThread = null;
            server = null;
            uploadMsg("Ftp server is stopped!");
        } else {
            uploadMsg("Ftp server dose not exist!");
        }
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            //通过放射获取 getWifiApState()方法
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            //调用getWifiApState() ，获取返回值
            int state = (int) method.invoke(manager);
            //通过放射获取 WIFI_AP的开启状态属性
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            //获取属性值
            int value = (int) field.get(manager);
            //判断是否开启
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

}