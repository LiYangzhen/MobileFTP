package com.example.mobileftp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import com.example.mobileftp.R;

public class MainActivity extends AppCompatActivity {

    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE};    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn1 = findViewById(R.id.btn_as_client);
        btn1.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this,ClientActivity.class);
            startActivity(i);
        });
        Button btn2 = findViewById(R.id.btn_as_server);
        btn2.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this,ServerActivity.class);
            startActivity(i);
        });
      TextView textView =  findViewById(R.id.tv_help_info);
        String builder =
                getString(R.string.help_title) +
                        "\n" +
                        getString(R.string.step1) +
                        "\n" +
                        getString(R.string.step2) +
                        "\n" +
                        getString(R.string.step3) +
                        "\n" +
                        getString(R.string.step4) +
                        "\n";
      textView.setText(builder);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }
}