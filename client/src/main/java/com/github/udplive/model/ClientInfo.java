package com.github.udplive.model;

import java.io.Serializable;
import java.net.InetAddress;

public class ClientInfo implements Serializable {

    public InetAddress ip;
    public int port;

    public ClientInfo(InetAddress ip, int port){
        this.ip = ip;
        this.port = port;
    }
}
