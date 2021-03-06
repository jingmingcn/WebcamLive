/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.github.udplive.server;

import com.github.udplive.model.ClientInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class App {

    static Map<String, ClientInfo> subscribes = new HashMap<String,ClientInfo>();
    static InetAddress uploaderIp = null;
    static int uploaderPort;

    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket(5666);
            long lastIdleCheck = System.currentTimeMillis();

            while(true){
                byte[] receiveData = new byte[1024*64];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                InetAddress ip = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String str = new String(receiveData).trim();
                long now = System.currentTimeMillis();
                System.out.println("Received:"+str);
                if(str.startsWith("LIVE")){
                    uploaderIp = ip;
                    uploaderPort = port;
                    System.out.println("Register Uploader "+uploaderIp.getHostAddress()+":"+uploaderPort);
                }else if(str.startsWith("WATCH")){
                    String s[] = str.split(";")[1].split(":");
                    subscribes.put(s[0]+":"+s[1],new ClientInfo(InetAddress.getByName(s[0]),Integer.parseInt(s[1])));
                    subscribes.put(ip.getHostAddress()+":"+port,new ClientInfo(ip,port));
                    byte[] sendData = new String(ip.getHostAddress()+":"+port).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,ip,port);
                    socket.send(sendPacket);
                }
                if(uploaderIp!=null){
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(subscribes);
                    byte[] sendData = baos.toByteArray();
                    baos.close();
                    //byte[] sendData = subscribes.keySet().stream().collect(Collectors.joining(";")).getBytes();
                    System.out.println("SEND SUBSCRIBES TO "+ uploaderIp.getHostName()+":"+uploaderPort+" # "+ new String(sendData));
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,uploaderIp,uploaderPort);
                    socket.send(sendPacket);
                }
//                if(now-lastIdleCheck>5_000){
//                    Map<String,Long> subscribes_ = new HashMap<String,Long>();
//                    subscribes.forEach((k,v)->{
//                        if(now-v<10_000){
//                            subscribes_.put(k,v);
//                        }
//                    });
//                    subscribes = subscribes_;
//                    lastIdleCheck = now;
//                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
