package com.example.mobileftp.Exceptions;

import java.io.IOException;

public class PortUsingException extends IOException {

    public PortUsingException(int port) {
        super("Port is be used for " + port);
    }

}
