package com.example.mobileftp.service;

import com.blankj.utilcode.util.PathUtils;

public final class FtpConfig {

    public static final String DEFAULT_USER = "test";
    public static final String DEFAULT_PASSWORD = "test";
    public static final Integer DEFAULT_PORT = 12345;
    public static final Integer DEFAULT_PASSIVE_PORT = 1025;
    public static final String SAVE_FILE_PATH = PathUtils.getExternalAppFilesPath();

}
