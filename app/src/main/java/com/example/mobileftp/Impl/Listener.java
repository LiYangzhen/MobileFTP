package com.example.mobileftp.Impl;

import com.example.mobileftp.interfaces.CallBack;

public class Listener {

    String value;
    CallBack callBack;

    public Listener(String value) {
        this.value = value;
    }

    public Listener(String value, CallBack callBack) {
        this.value = value;
        this.callBack = callBack;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        callBack.callBack(value);
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

}
