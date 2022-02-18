package com.example.mobileftp.Exceptions;

import java.io.IOException;

public class NotInitException extends IOException {

    public NotInitException() {
        super("DTP has not initialized");
    }

}
