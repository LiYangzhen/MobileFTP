package com.example.mobileftp.Exceptions;

import java.io.IOException;

public class ServerBreakDownException extends IOException {

    public ServerBreakDownException() {
        super("Server Break Down");
    }
}
